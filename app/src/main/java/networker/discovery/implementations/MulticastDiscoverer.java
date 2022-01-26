package networker.discovery.implementations;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import networker.RoomKnowledge;
import networker.discovery.PeerDiscoverer;
import networker.discovery.PeerReceiver;
import networker.discovery.PeerSender;
import networker.discovery.PeerServer;
import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkInformation;
import networker.helpers.NetworkUtilities;
import networker.peers.Peer;
import networker.peers.User;
import networker.sockets.ServerSocketAdapter;

/** Requires multicast permissions.
 * Requires acquiring WifiManager.MulticastLock.
 * Even then, depending on the device this might not work (it will either fail silently,
 * or with a bang depending on the device, we cannot know).
 * source:
 * @link https://codeisland.org/2012/udp-multicast-on-android
 */
public class MulticastDiscoverer implements PeerDiscoverer {
    private static final int SO_TIMEOUT_MILLIS = 5_000;
    private static final int SS_TIMEOUT_MILLIS = 5_000;

    private final PeerReceiver receiver;
    private final PeerSender sender;
    private final PeerServer inboundServer;

    private final MulticastSocket socket;
    private final ServerSocketAdapter serverSocket;
    private final NetworkInformation netInfo;

    private final RoomKnowledge room;

    public MulticastDiscoverer(PeerReceiver recv, PeerSender sendr, PeerServer peerServer,
                               ServerSocketAdapter ss, MulticastSocket ms, NetworkInformation info,
                               RoomKnowledge roomKnowledge) throws SocketException {
        receiver = recv;
        sender = sendr;
        inboundServer = peerServer;

        socket = ms;
        serverSocket = ss;
        room = roomKnowledge;

        netInfo = info;
        // FOR COMPLETENESS SAKE
        socket.setSoTimeout(SO_TIMEOUT_MILLIS);
        serverSocket.setSoTimeout(SS_TIMEOUT_MILLIS);
    }

    public List<User> processOnce() throws IOException {
        Queue<String> groupBroadcasts = receiver.discoverPeers(socket, SO_TIMEOUT_MILLIS);

        LinkedList<User> usersFound = new LinkedList<>();
        for (String bCast : groupBroadcasts) {

            try {
                usersFound.add(NetworkUtilities.processUserSalutationJson(bCast));
            } catch (JSONException | UnknownHostException | InvalidPortValueException e) {
                Log.d("networker", bCast, e);
            }
        }

        for (User u : usersFound) {
            processFoundUser(u);
        }

        sender.announce(socket, netInfo);

        inboundServer.listen(serverSocket, SS_TIMEOUT_MILLIS, room);

        return usersFound;
    }

    private void processFoundUser(User u) throws IOException {
        // if this is a completely new peer...
        if (!room.hasPeer(u)) {
            processNewPeer(u);
        } else {
            // if this peer already exists...
            processExistingPeer(u);
        }
    }

    private void processNewPeer(User u) throws IOException {
        room.addPeer(new Peer(u));

        try {
            u.createUserSocket();
        } catch (InvalidPortValueException e) {
            Log.d("networker", "u.createUserSocket()", e);
        }
    }

    private void processExistingPeer(User u) throws IOException {
        User uExisting = room.getPeer(u.getIDENTIFIER()).getUser();
        if (uExisting.equals(u)) return; //ignore if it's completely the same and nothing changed

        //update existing information if anything changed
        if (uExisting.updateSelf(u)) {
            try {
                //update socket if port changed
                uExisting.createUserSocket();
            } catch (InvalidPortValueException e) {
                Log.d("networker", "u.createUserSocket() 2", e);
            }
        }
    }

}

package networker.discovery.discoverers;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import networker.RoomKnowledge;
import networker.discovery.io.announcers.PeerAnnouncer;
import networker.discovery.io.receivers.PeerReceiver;
import networker.discovery.servers.PeerServer;
import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkInformation;
import networker.helpers.NetworkUtilities;
import networker.peers.Peer;
import networker.peers.User;
import networker.sockets.ServerSocketAdapter;

/**
 * ---------------------------- For multicast sender/receiver ----------------------------
 * Requires multicast permissions.
 * <p>
 * Requires acquiring WifiManager.MulticastLock.
 * Even then, depending on the device this might not work (it will either fail silently,
 * or with a bang depending on the device, we cannot know).
 * <p>
 * Requires the MulticastSocket to have joined the multicastDiscoverGroup of NetworkInformation
 * before passing it (with .joinGroup (SocketAddress mcastaddr, NetworkInterface netIf)
 * <p>
 * ---------------------------- For broadcast sender/receiver ----------------------------
 * <p>
 * N/A, currently doesn't require anything (there is no such impl yet)
 * <p>
 * source:
 *
 * @link https://codeisland.org/2012/udp-multicast-on-android
 */
public class MulticastGroupPeerDiscoverer implements PeerDiscoverer {
    private static final int BO_TIMEOUT_MILLIS_HIGH_SPEED = 10;
    private static final int BO_TIMEOUT_MILLIS = 500;
    private static final int SS_TIMEOUT_MILLIS = 100;
    private static final int SS_SO_TIMEOUT_MILLIS = 50;
    private final int BO_TIMEOUT_MILLIS_HIGH_SPEED_DURATION;

    private final PeerReceiver receiver;
    private final PeerAnnouncer sender;
    private final PeerServer inboundServer;

    private final DatagramSocket udpSocket;
    private final ServerSocketAdapter serverSocket;
    private final NetworkInformation netInfo;

    private final RoomKnowledge room;

    public MulticastGroupPeerDiscoverer(PeerReceiver recv, PeerAnnouncer sendr, PeerServer peerServer,
                                        ServerSocketAdapter ss, DatagramSocket ds, NetworkInformation info,
                                        RoomKnowledge roomKnowledge, int HIGH_SPEED_MILLIS) {
        receiver = recv;
        sender = sendr;
        inboundServer = peerServer;

        udpSocket = ds;
        serverSocket = ss;
        room = roomKnowledge;

        netInfo = info;
        // FOR COMPLETENESS SAKE

        BO_TIMEOUT_MILLIS_HIGH_SPEED_DURATION = HIGH_SPEED_MILLIS;
    }

    @Override
    public void highSpeedDiscovery() throws IOException {
        long timeSpent = 0;
        final long start = System.currentTimeMillis();
        Queue<String> groupBroadcasts = new LinkedList<>();
        udpSocket.setSoTimeout(BO_TIMEOUT_MILLIS_HIGH_SPEED);

        while (timeSpent < BO_TIMEOUT_MILLIS_HIGH_SPEED_DURATION) {
            sender.announce(udpSocket, netInfo);

            groupBroadcasts.addAll(receiver.discoverPeers(udpSocket, BO_TIMEOUT_MILLIS_HIGH_SPEED));

            long end = System.currentTimeMillis();
            timeSpent = end - start;
        }

        LinkedList<User> usersFound = filterValidJSON(groupBroadcasts);
        processUsers(usersFound);
    }

    //TODO: rework this, so we're constantly listening the mcast in a new thread,
    // and set the announce and inbound server listen on a different thread
    @Override
    public void processOnce() throws IOException {
        udpSocket.setSoTimeout(BO_TIMEOUT_MILLIS);
        serverSocket.setSoTimeout(SS_TIMEOUT_MILLIS);

        sender.announce(udpSocket, netInfo);
        inboundServer.listen(serverSocket, SS_SO_TIMEOUT_MILLIS, room);
        Queue<String> groupBroadcasts = receiver.discoverPeers(udpSocket, BO_TIMEOUT_MILLIS);

        LinkedList<User> usersFound = filterValidJSON(groupBroadcasts);
        processUsers(usersFound);
    }

    private LinkedList<User> filterValidJSON(Queue<String> groupBroadcasts) {
        LinkedList<User> usersFound = new LinkedList<>();
        for (String bCast : groupBroadcasts) {
            try {
                Log.d("networker.discovery.discoverers.filterValidJSON", "processOnce: " + bCast);
                usersFound.add(NetworkUtilities.processUserSalutationJson(bCast));
            } catch (JSONException | UnknownHostException | InvalidPortValueException e) {
                Log.d("networker.discovery.discoverers.filterValidJSON", bCast, e);
            }
        }
        return usersFound;
    }

    private void processUsers(LinkedList<User> usersFound) {
        for (User u : usersFound) {
            try {
                Log.d("networker.discovery.discoverers.processUsers", "FOUND AND PROCESSING " + u.getUsername());
                processFoundUser(u);
            } catch (IOException e) {
                Log.e("networker.discovery.discoverers.processUsers", e.getMessage(), e);
            }
        }
    }

    private void processFoundUser(User u) throws IOException {
        // corner case: ourself
        if (netInfo.getOurselves().equals(u)) return;

        // if this is a completely new peer...
        if (!room.hasPeer(u)) {
            Log.d("networker.discovery.discoverers.processFoundUser", "FOUND AND PROCESSING COMPLETELY NEW PEER " + u.getUsername());
            processNewPeer(u);
        } else {
            Log.d("networker.discovery.discoverers.processFoundUser", "FOUND AND PROCESSING EXISTING PEER " + u.getUsername());
            // if this peer already exists...
            processExistingPeer(u);
        }
    }

    private void processNewPeer(User u) throws IOException {
        try {
            u.createUserSocket();
            room.addPeer(new Peer(u));
        } catch (InvalidPortValueException | InterruptedException e) {
            Log.d("networker.discovery.discoverers.processNewPeer", "u.createUserSocket()", e);
        }
    }

    private void processExistingPeer(User u) {
        User uExisting = room.getPeer(u.getIDENTIFIER()).getUser();
        if (uExisting.equals(u)) return; //ignore if it's completely the same and nothing changed

        uExisting.setStatus(u.getStatus()); // set the status to the new one

    }

}

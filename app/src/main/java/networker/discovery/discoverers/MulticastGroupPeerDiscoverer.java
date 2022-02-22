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
    private static final int BO_TIMEOUT_HIGH_SPEED_MILLIS = 20;
    private static final int BO_TIMEOUT_MILLIS = 5000;
    private static final int SS_TIMEOUT_MILLIS = 15000;
    private final int BO_TIMEOUT_MILLIS_HIGH_SPEED_DURATION;

    private final PeerReceiver receiver;
    private final PeerAnnouncer sender;
    private final PeerServer inboundServer;

    private final DatagramSocket udpSocket;
    private final ServerSocketAdapter serverSocket;
    private final NetworkInformation netInfo;

    private final RoomKnowledge rk;

    public MulticastGroupPeerDiscoverer(PeerReceiver recv, PeerAnnouncer sendr, PeerServer peerServer,
                                        ServerSocketAdapter ss, DatagramSocket ds, NetworkInformation info,
                                        RoomKnowledge roomKnowledge, int HIGH_SPEED_MILLIS) {
        receiver = recv;
        sender = sendr;
        inboundServer = peerServer;

        udpSocket = ds;
        serverSocket = ss;
        rk = roomKnowledge;

        netInfo = info;

        // FOR COMPLETENESS SAKE
        BO_TIMEOUT_MILLIS_HIGH_SPEED_DURATION = HIGH_SPEED_MILLIS;
    }

    /** This method is blocking, and should be run in a thread in a loop. */
    @Override
    public void highSpeedDiscover() throws IOException {
        long timeSpent = 0;
        final long start = System.currentTimeMillis();
        Queue<String> groupBroadcasts = new LinkedList<>();
        udpSocket.setSoTimeout(BO_TIMEOUT_HIGH_SPEED_MILLIS);

        while (timeSpent < BO_TIMEOUT_MILLIS_HIGH_SPEED_DURATION) {
            sender.announce(udpSocket, netInfo);

            groupBroadcasts.addAll(receiver.discoverPeers(udpSocket, BO_TIMEOUT_HIGH_SPEED_MILLIS));

            long end = System.currentTimeMillis();
            timeSpent = end - start;
        }

        LinkedList<User> usersFound = filterValidJSON(groupBroadcasts);
        processUsers(usersFound);
    }

    /** This method is blocking, and should be run in a thread in a loop. */
    public void discover() throws IOException {
        udpSocket.setSoTimeout(BO_TIMEOUT_MILLIS);
        sender.announce(udpSocket, netInfo);
        Queue<String> groupBroadcasts = receiver.discoverPeers(udpSocket, BO_TIMEOUT_MILLIS);

        LinkedList<User> usersFound = filterValidJSON(groupBroadcasts);
        processUsers(usersFound);
    }

    @Override
    public void listen() throws IOException {
        serverSocket.setSoTimeout(SS_TIMEOUT_MILLIS);
        inboundServer.listen(serverSocket, SS_TIMEOUT_MILLIS, rk);
    }

    private LinkedList<User> filterValidJSON(Queue<String> groupBroadcasts) {
        LinkedList<User> usersFound = new LinkedList<>();
        for (String bCast : groupBroadcasts) {
            try {
                Log.d("networker.discovery.discoverers.filterValidJSON", "processOnce: " + bCast);
                usersFound.add(NetworkUtilities.processUserSalutationJSON(bCast));
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
                u.lock();
                processFoundUser(u);
            } catch (IOException e) {
                Log.d("networker.discovery.discoverers.processUsers", e.getMessage(), e);
            } catch (InterruptedException e) {
                Log.e("networker.discovery.discoverers.processUsers", e.getMessage(), e);
            } finally {
                u.unlock();
            }
        }
    }

    private void processFoundUser(User u) throws IOException {
        // corner case: ourself
        if (netInfo.getOurselves().equals(u)) return;

        // if this is a completely new peer...
        if (!rk.hasPeer(u)) {
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
            //FIXME corner case with same num, just sum the address of the user and compare to our sum, whichever is greater has priority
            if (netInfo.getOurselves().getPriority() < u.getPriority()) {
                Log.e("networker.discovery.discoverers.processNewPeer", "user has priority over us " + u.getIDENTIFIER());
                u.createUserSocket();
                Log.e("networker.discovery.discoverers.processNewPeer", "Connected to " + u.getIDENTIFIER() + " " + u.getAddress());
                rk.addPeer(new Peer(u));
            }
        } catch (InvalidPortValueException e) {
            Log.d("networker.discovery.discoverers.processNewPeer", "u.createUserSocket()", e);
        }
    }

    private void processExistingPeer(User u) {
        User uExisting = rk.getPeer(u.getIDENTIFIER()).getUser();
        try {
            uExisting.lock();

            if (uExisting.equals(u)) return; //ignore if it's completely the same and nothing changed

            uExisting.setStatus(u.getStatus()); // set the status to the new one
        } catch (InterruptedException e) {
            Log.e("networker.discovery.discoverers.processExistingPeer", "uExisting.lock()", e);
        } finally {
            uExisting.unlock();
        }
    }

}

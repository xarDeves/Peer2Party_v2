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
import networker.peers.user.User;
import networker.peers.user.network.Networking;
import networker.peers.user.synchronization.Synchronization;
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
    private static final String TAG = "networker.discovery.discoverers:MulticastGroupPeerDiscoverer";

    private static final int BO_TIMEOUT_HIGH_SPEED_MILLIS = 20;
    private static final int BO_TIMEOUT_MILLIS = 5000;
    private static final int SS_TIMEOUT_MILLIS = 15000;
    private static final int SS_SO_TIMEOUT_MILLIS = 1000;
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

        LinkedList<User> usersFound = filterValidUsers(groupBroadcasts);
        processUsers(usersFound);
    }

    /** This method is blocking, and should be run in a thread in a loop. */
    public void discover() throws IOException {
        udpSocket.setSoTimeout(BO_TIMEOUT_MILLIS);
        sender.announce(udpSocket, netInfo);
        Queue<String> groupBroadcasts = receiver.discoverPeers(udpSocket, BO_TIMEOUT_MILLIS);

        LinkedList<User> usersFound = filterValidUsers(groupBroadcasts);
        processUsers(usersFound);
    }

    @Override
    public void listen() throws IOException {
        serverSocket.setSoTimeout(SS_TIMEOUT_MILLIS);
        inboundServer.listen(serverSocket, SS_TIMEOUT_MILLIS, SS_SO_TIMEOUT_MILLIS);
    }

    private LinkedList<User> filterValidUsers(Queue<String> groupBroadcasts) {
        LinkedList<User> usersFound = new LinkedList<>();
        for (String bCast : groupBroadcasts) {
            try {
                Log.v(TAG + ".filterValidUsers", "Processing " + bCast);
                usersFound.add(NetworkUtilities.processUserSalutationJSON(bCast));
            } catch (JSONException | UnknownHostException | InvalidPortValueException e) {
                Log.e(TAG + ".filterValidUsers", bCast, e);
            }
        }
        return usersFound;
    }

    private void processUsers(LinkedList<User> usersFound) {
        for (User u : usersFound) {
            Synchronization sync = u.getSynchronization();
            try {
                sync.lock();
                Log.d(TAG + ".processUsers", "Found & processing " + u.getUsername());
                processUser(u);
                Log.d(TAG + ".processUsers", "Finished processing " + u.getUsername());
            } catch (IOException | InterruptedException e) {
                Log.e(TAG + ".processUsers", "", e);
            } finally {
                sync.unlock();
            }
        }
    }

    private void processUser(User u) throws IOException {
        // corner case: ourself
        if (netInfo.getOurselves().equals(u)) return;

        // if this is a completely new peer...
        if (!rk.hasPeer(u)) {
            processNewPeer(u);
            return;
        }

        // if this peer already exists...
        processExistingPeer(u);
    }

    private void processNewPeer(User u) throws IOException {
        try {
            //FIXME corner case with same num, just sum the address of the user and compare to our sum, whichever is greater has priority
            Networking net = u.getNetworking();
            Log.e(TAG + ".processNewPeer", "New peer has priority "+ net.getPriority() +
                    ", we have "+ netInfo.getOurselves().getNetworking().getPriority());
            if (netInfo.getOurselves().getNetworking().getPriority() < net.getPriority()) {
                Log.d(TAG + ".processNewPeer", u.getUsername() + " has priority over us, creating socket");
                net.createUserSocket();
                rk.addPeer(new Peer(u));
            }
        } catch (InvalidPortValueException e) {
            Log.e(TAG + ".processNewPeer", "User invalid port value " + u.getNetworking().getPort(), e);
        }
    }

    private void processExistingPeer(User u) {
        User uExisting = rk.getPeer(u).getUser();
        Synchronization sync = uExisting.getSynchronization();
        try {
            sync.lock();
            //update status & priority
            uExisting.setStatus(u.getStatus());
            uExisting.getNetworking().setPriority(u.getNetworking().getPriority());
        } catch (InterruptedException e) {
            Log.e(TAG + ".processExistingPeer", "", e);
        } finally {
            sync.unlock();
        }
    }

}

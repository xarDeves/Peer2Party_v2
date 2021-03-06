package networker.discovery.discoverers;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
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

    private final int BO_TIMEOUT_MILLIS_HIGH_SPEED_DURATION;
    private static final int BO_TIMEOUT_HIGH_SPEED_MILLIS = 20;
    private static final int BO_TIMEOUT_MILLIS = 5_000;
    private static final int SS_TIMEOUT_MILLIS = 15_000;
    private static final int SS_SO_TIMEOUT_MILLIS = 500;

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

    @Override
    public void listen() throws IOException {
        serverSocket.setSoTimeout(SS_TIMEOUT_MILLIS);
        inboundServer.listen(serverSocket, SS_TIMEOUT_MILLIS, SS_SO_TIMEOUT_MILLIS);
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
            } catch (IOException e) {
                Log.e(TAG + ".processUsers", "", e);
                try {
                    u.getNetworking().shutdown();
                } catch (IOException ioException) {
                    Log.e(TAG + ".processUsers", "Shutdown failed when IOException is thrown?", ioException);
                }
            } catch (InterruptedException e) {
                Log.e(TAG + ".processUsers", "", e);
            } finally {
                sync.unlock();
            }
        }
    }

    private void processUser(User u) throws IOException {
        // corner case: ourself
        if (netInfo.isOurself(u)) return;

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
            Networking newUserNetworker = u.getNetworking();
            if (newUserNetworker.hasPriority(netInfo.getOurselves())) {
                Log.d(TAG + ".processNewPeer", u.getUsername() + " has priority over us, creating socket");
                newUserNetworker.createUserSocket();
                NetworkUtilities.sendSalutation(u, netInfo.getOurselves());
                rk.addPeer(new Peer(u));
            }
        } catch (SocketTimeoutException e) {
            Log.e(TAG + ".processNewPeer", "Timeout exception for " + u.getUsername(), e);
        } catch (JSONException e) {
            Log.e(TAG + ".processNewPeer", "Invalid json from ourself", e);
        }
    }

    private void processExistingPeer(User u) {
        User uExisting = rk.getPeer(u).getUser();
        Synchronization sync = uExisting.getSynchronization();
        try {
            // lock the already existing user, since we'll be reusing the existing variable in-memory
            sync.lock();
            //update status & priority
            uExisting.setStatus(u.getStatus());
            NetworkUtilities.createConnectionIfThereIsNone(uExisting, netInfo.getOurselves());
        } catch (SocketTimeoutException e) {
            //should we do something particular in this situation?
            Log.e(TAG + ".processExistingPeer", "Timeout exception when creating conn if there's none " + u.getUsername(), e);
        } catch (JSONException | InterruptedException | InvalidPortValueException e) {
            Log.e(TAG + ".processExistingPeer", "", e);
        } catch (IOException e) {
            Log.e(TAG + ".processExistingPeer", "", e);
            try {
                uExisting.getNetworking().shutdown();
            } catch (IOException ioException) {
                Log.e(TAG + ".processExistingPeer", "Shutdown failed when IOException is thrown?", e);
            }
        } finally {
            sync.unlock();
        }
    }

}

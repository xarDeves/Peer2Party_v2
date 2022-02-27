package networker.discovery.servers;

import android.util.Log;

import org.json.JSONException;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

import networker.RoomKnowledge;
import networker.exceptions.InconsistentDataException;
import networker.exceptions.InvalidPortValueException;
import networker.exceptions.SelfConnectionException;
import networker.helpers.NetworkInformation;
import networker.helpers.NetworkUtilities;
import networker.peers.Peer;
import networker.peers.user.User;
import networker.peers.user.network.Networking;
import networker.peers.user.synchronization.Synchronization;
import networker.sockets.ServerSocketAdapter;
import networker.sockets.SocketAdapter;

public class InboundConnectionServer implements PeerServer {
    private static final String TAG = "networker.discovery.servers:InboundConnectionServer";

    private final NetworkInformation netInfo;
    private final RoomKnowledge rk;

    public InboundConnectionServer(NetworkInformation networkInformation, RoomKnowledge roomKnowledge) {
        netInfo = networkInformation;
        rk = roomKnowledge;
    }

    @Override
    public void listen(ServerSocketAdapter ss, final int timeToReceiveMillis, final int soTimeToReceiveMillis) throws IOException {
        long timeSpent = 0;
        final long start = System.currentTimeMillis();

        while (timeSpent < timeToReceiveMillis) {
            try {
                handleIndividualClient(ss, soTimeToReceiveMillis);
                Log.d(TAG + ".listen", "Finished handling user!");
            } catch (SocketTimeoutException e) {
                Log.v(TAG + ".listen", "", e);
            }

            long end = System.currentTimeMillis();
            timeSpent = end - start;

            Log.d(TAG + ".listen", "server time spent " + timeSpent);
        }
    }

    private void handleIndividualClient(ServerSocketAdapter ss, final int soTimeToReceiveMillis) throws IOException {
        SocketAdapter client = ss.accept();
        Log.d(TAG + ".handleIndividualClient", "Found user!");
        client.setTimeout(soTimeToReceiveMillis);

        String salutation = getSalutation(client.getDataInputStream());

        User user = null;
        try {
            user = NetworkUtilities.processUserSalutationJSON(salutation);
            user.getSynchronization().lock();
            Log.d(TAG + ".handleIndividualClient", "Processed salutation! " + salutation + " Handling...");
            handleUser(user, client);
        } catch (JSONException | InvalidPortValueException e) {
            Log.e(TAG + ".handleIndividualClient", "User failed verification check", e);
            client.close(); // forfeit the connection if client sent invalid data, there's clearly an issue
        } catch (InterruptedException | SelfConnectionException | InconsistentDataException e) {
            Log.e(TAG + ".handleIndividualClient", "", e);
        } finally {
            if (user!=null) user.getSynchronization().unlock();
        }
    }

    private void handleUser(User u, SocketAdapter s) throws IOException, InconsistentDataException, SelfConnectionException {
        if (!NetworkUtilities.userIsConsistentToSocket(u, s)) throw new InconsistentDataException();
        if (netInfo.isOurself(u)) throw new SelfConnectionException();

        if (!rk.hasPeer(u)) {
            handleNewPeer(u, s);
            return;
        }

        handleExistingPeer(rk.getPeer(u).getUser(), s);
    }

    private void handleNewPeer(User u, SocketAdapter s) throws IOException {
        u.getNetworking().replaceSocket(s);
        rk.addPeer(new Peer(u));
        Log.d(TAG + ".handleNewPeer", "Added user " + u.getIDENTIFIER());
    }

    private void handleExistingPeer(User u, SocketAdapter s) throws IOException {
        Networking net = u.getNetworking();
        Synchronization sync = u.getSynchronization();
        try {
            sync.lock();
            //for some reason, an already known peer tries to refresh the connection with us, perhaps his side of comms died
            // refresh the connection even if the connection's fine on our end
            net.replaceSocket(s);
            Log.d(TAG + ".handleExistingPeer", "Replaced socket of existing user " + u.getUsername());
        } catch (InterruptedException e) {
            Log.e(TAG + ".handleExistingPeer", "", e);
        } finally {
            sync.unlock();
        }
    }

    private String getSalutation(DataInputStream is) throws IOException {
        byte[] buffer = new byte[NetworkUtilities.DISCOVERY_BUFFER_SIZE];

        try {
            int bRead = is.read(buffer);
            Log.d(TAG + ".getSalutation", "bRead " + bRead);
        } catch (SocketTimeoutException e) {
            Log.e(TAG + ".getSalutation", "", e);
            return "";
        }
        return NetworkUtilities.convertBytesToUTF8String(buffer);
    }
}

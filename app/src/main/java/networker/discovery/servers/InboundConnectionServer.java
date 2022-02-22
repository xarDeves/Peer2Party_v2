package networker.discovery.servers;

import android.util.Log;

import org.json.JSONException;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

import networker.RoomKnowledge;
import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkUtilities;
import networker.peers.Peer;
import networker.peers.User;
import networker.sockets.ServerSocketAdapter;
import networker.sockets.SocketAdapter;

public class InboundConnectionServer implements PeerServer {

    @Override
    public void listen(ServerSocketAdapter ss, final int timeToReceiveMillis, RoomKnowledge room) throws IOException {

        long timeSpent = 0;
        final long start = System.currentTimeMillis();

        while (timeSpent < timeToReceiveMillis) {
            try {
                handleIndividualClient(ss, room, timeToReceiveMillis);
            } catch (SocketTimeoutException e) {
                Log.v("networker.discovery.servers.listen", "SocketTimeoutException" , e);
            }

            long end = System.currentTimeMillis();
            timeSpent = end - start;

            Log.d("networker.discovery.servers.listen", "current time spent server " + timeSpent);
            Log.d("networker.discovery.servers.listen", "server log " + ss.log());
        }
    }

    private void handleIndividualClient(ServerSocketAdapter ss, RoomKnowledge room,
                                        final int timeToReceiveMillis) throws IOException {
        SocketAdapter client = ss.accept();
        client.setTimeout(timeToReceiveMillis);

        String salutation = getSalutation(client);

        User u = null;
        try {
            u = NetworkUtilities.processUserSalutationJSON(salutation);
            u.lock();
            handleUser(u, client, room);
        } catch (JSONException | InvalidPortValueException e) {
            Log.d("networker.discovery.servers.handleIndividualClient", salutation, e);
            client.close(); // forfeit the connection if client sent invalid data, there's clearly an issue
        } catch (InterruptedException e) {
            Log.e("networker.discovery.servers.handleIndividualClient", salutation, e);
        } finally {
            if (u!=null) u.unlock();
        }
    }

    private void handleUser(User u, SocketAdapter client, RoomKnowledge room) throws IOException {
        //for some reason the user sent data that is inconsistent with the socket he's contacting us from
        if (!NetworkUtilities.userDataIsConsistentToSocket(client, u)) return;

        if (!room.hasPeer(u)) {
            u.replaceSocket(client);
            room.addPeer(new Peer(u));
            Log.d("networker.discovery.servers.handleUser", "Added socket to new user " + u.getIDENTIFIER());
            return;
        }

        if (room.getPeer(u.getIDENTIFIER()).getUser().connectionIsUsable()) {
            User knownUser = room.getPeer(u.getIDENTIFIER()).getUser();
            try {
                knownUser.lock();
                //for some reason, an already known peer tries to refresh the connection with us, perhaps his side of comms died
                // refresh the connection even if the connection's fine on our end
                Log.d("networker.discovery.servers.handleUser", "Replacing socket of user port is valid "
                        + knownUser.portIsValid() + " and closed " + knownUser.socketIsClosed());
                knownUser.replaceSocket(client);
                Log.d("networker.discovery.servers.handleUser", "Replaced socket of user id " + knownUser.getIDENTIFIER());
            } catch (InterruptedException e) {
                Log.e("networker.discovery.servers.handleUser", e.getMessage(), e);
            } finally {
                knownUser.unlock();
            }
        }
    }

    private String getSalutation(SocketAdapter socket) throws IOException {

        byte[] buffer = new byte[NetworkUtilities.DISCOVERY_BUFFER_SIZE];

        try {
            DataInputStream is = socket.getDataInputStream();
            is.read(buffer);
        } catch (SocketTimeoutException e) {
            return "";
        }
        return NetworkUtilities.convertBytesToUTF8String(buffer);
    }
}

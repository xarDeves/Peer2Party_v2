package networker.discovery.implementations;

import android.util.Log;

import org.json.JSONException;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

import networker.RoomKnowledge;
import networker.discovery.PeerServer;
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
        long startTime = System.currentTimeMillis();

        while (timeSpent < timeToReceiveMillis) {
            try {
                timeSpent = handleIndividualClient(ss, room, timeToReceiveMillis, startTime);
            } catch (SocketTimeoutException e) {
                Log.d("networker", "SocketTimeoutException", e);
            }

            Log.d("networker", "current time spent server " + timeSpent);
        }
    }

    private long handleIndividualClient(ServerSocketAdapter ss, RoomKnowledge room,
                                        final int timeToReceiveMillis, final long start) throws IOException {
        SocketAdapter client = ss.accept();
        client.setTimeout(timeToReceiveMillis);

        String salutation = getSalutation(client);

        long end = System.currentTimeMillis();
        long timeSpent = end - start;

        try {
            User u = NetworkUtilities.processUserSalutationJson(salutation);
            handleUser(u, client, room);
        } catch (JSONException | InvalidPortValueException e) {
            Log.d("networker", salutation, e);
            client.close(); // forfeit the connection if client sent invalid data, there's clearly an issue
        }

        return timeSpent;
    }

    private void handleUser(User u, SocketAdapter client, RoomKnowledge room) throws IOException {
        //for some reason the user sent data that is inconsistent with the socket he's contacting us from
        if (!NetworkUtilities.userDataIsConsistentToSocket(client, u)) return;

        if (room.hasPeer(u) && room.getPeer(u.getIDENTIFIER()).getUser().connectionIsUsable()) {
            User knownUser = room.getPeer(u.getIDENTIFIER()).getUser();

            //for some reason, an already known peer tries to refresh the connection with us, perhaps his side of comms died
            if (knownUser.connectionIsUsable()) {
                knownUser.shutdownUser(); // refresh the connection even if the connection's fine
            }

            knownUser.replaceSocket(client);
            knownUser.updateSelf(u);
        }

        if (!room.hasPeer(u)) room.addPeer(new Peer(u));
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

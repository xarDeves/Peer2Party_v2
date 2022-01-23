package networker.discovery;

import android.util.Log;

import org.json.JSONException;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

import networker.RoomKnowledge;
import networker.discovery.interfaces.PeerServer;
import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkUtilities;
import networker.peers.Peer;
import networker.peers.User;
import networker.sockets.ServerSocketAdapter;
import networker.sockets.SocketAdapter;

public class InboundConnectionServer implements PeerServer {

    byte[] buffer = new byte[NetworkUtilities.DISCOVERY_BUFFER_SIZE];

    @Override
    public void listen(ServerSocketAdapter ss, int timeToReceiveMillis, RoomKnowledge room) throws IOException {
        try {
            long timeSpent = 0;
            long start = System.currentTimeMillis();

            while (timeSpent < timeToReceiveMillis) {
                SocketAdapter client = ss.accept();
                client.setTimeout(timeToReceiveMillis);

                String salutation = getSalutation(client);

                long end = System.currentTimeMillis();
                timeSpent = end - start;

                try {
                    User u = NetworkUtilities.processUserSalutationJson(salutation);

                    if (room.hasPeer(u) && !u.connectionIsUsable()) room.getPeer(u.getIDENTIFIER()).getUser().replaceSocket(client);
                    if (!room.hasPeer(u)) room.addPeer(new Peer(u));

                    //we don't care in the case we have the peer with a working connection
                } catch (JSONException | InvalidPortValueException e) {
                    Log.d("networker", salutation, e);
                    client.close(); // forfeit the connection if client sent invalid data, there's clearly an issue
                }

                Log.d("networker", "current time spent server " + timeSpent);
            }
        } catch (SocketTimeoutException e) {
            Log.d("networker", "SocketTimeoutException", e);
        }
    }

    public String getSalutation(SocketAdapter socket) throws IOException {
        try {
            DataInputStream is = new DataInputStream(socket.getInputStream());
            is.read(buffer);
        } catch (SocketTimeoutException e) {
            return "";
        }
        return NetworkUtilities.convertBytesToUTF8String(buffer);
    }
}

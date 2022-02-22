package networker.peers.user;

import android.util.Log;

import org.json.JSONException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkUtilities;
import networker.peers.user.network.Networking;
import networker.sockets.SocketAdapter;

class Networker implements Networking {

    private final User user;

    private final InetAddress address;
    private final int port;
    private int priority;

    private SocketAdapter currentUserSocket = null;

    Networker(InetAddress address, int port, int priority, User u) {
        this.address = address;
        this.port = port;

        this.priority = priority;

        user = u;
    }

    @Override
    public void createUserSocket() throws IOException, InvalidPortValueException {
        if (!portIsValid()) throw new InvalidPortValueException();
        shutdown();
        //FIXME add timeout to socket creation
        currentUserSocket = new SocketAdapter(address, port);
        Log.d("networker.peers.user.User.createUserSocket", "connected to " + currentUserSocket.log());

        try {
            sendSalutation();
        } catch (JSONException e) {
            Log.e("networker.peers.user.User.createUserSocket", "sendSalutation() ", e);
        }
    }

    @Override
    public boolean connectionIsUsable() {
        return currentUserSocket != null && !currentUserSocket.isClosed();
    }

    @Override
    public void replaceSocket(SocketAdapter newSocket) throws IOException {
        shutdown();
        currentUserSocket = newSocket;
        Log.d("networker.peers.user.User.replaceSocket", "connected to " + currentUserSocket.log());
    }

    @Override
    public SocketAdapter getCurrentUserSocket() {
        return currentUserSocket;
    }

    @Override
    public boolean portIsValid() {
        return NetworkUtilities.portIsValid(port);
    }

    @Override
    public boolean socketIsClosed() {
        return currentUserSocket.isClosed();
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public InetAddress getAddress() {
        return address;
    }

    @Override
    public String getHostAddress() {
        return address.getHostAddress();
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int p) {
        priority = p;
    }

    void sendSalutation() throws IOException, JSONException {
        DataOutputStream dos = currentUserSocket.getDataOutputStream();
        dos.write(NetworkUtilities.convertUTF8StringToBytes(NetworkUtilities.getUserSalutationJSON(user).toString()));
        dos.flush();
    }

    void shutdown() throws IOException {
        if (currentUserSocket != null) {
            if (!currentUserSocket.isClosed()) currentUserSocket.close();
        }
    }
}

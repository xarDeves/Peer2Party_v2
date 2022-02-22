package networker.peers.user;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkUtilities;
import networker.peers.user.network.Networking;
import networker.sockets.SocketAdapter;

class Networker implements Networking {
    private static final int SO_TIMEOUT = 2000;

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
        currentUserSocket = new SocketAdapter();
        currentUserSocket.connect(new InetSocketAddress(address, port), SO_TIMEOUT);
        currentUserSocket.setTimeout(SO_TIMEOUT);
        Log.d("networker.peers.user.User.createUserSocket", "connected to " + currentUserSocket.log());
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

    @Override
    public void shutdown() throws IOException {
        if (currentUserSocket != null) {
            if (!currentUserSocket.isClosed()) currentUserSocket.close();
        }
    }
}

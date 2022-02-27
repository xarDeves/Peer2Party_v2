package networker.peers.user;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import networker.peers.user.network.Networking;
import networker.sockets.SocketAdapter;

class Networker implements Networking {
    private static final String TAG = "networker.peers.user:Networker";
    private static final int SO_TIMEOUT = 2000;

    private final InetAddress address;
    private final int port;
    private int priority;

    private SocketAdapter currentUserSocket = null;

    Networker(InetAddress address, int port, int priority) {
        this.address = address;
        this.port = port;
        this.priority = priority;
    }

    @Override
    public void createUserSocket() throws IOException {
        shutdown();
        currentUserSocket = new SocketAdapter();
        InetSocketAddress soAddr = new InetSocketAddress(address, port);
        currentUserSocket.connect(soAddr, SO_TIMEOUT);
        currentUserSocket.setTimeout(SO_TIMEOUT);
        //FIXME add socket timeout catch exception to all callees
    }

    @Override
    public boolean connectionIsUsable() {
        return currentUserSocket != null && !currentUserSocket.isClosed();
    }

    @Override
    public void replaceSocket(SocketAdapter newSocket) throws IOException {
        shutdown();
        currentUserSocket = newSocket;
        currentUserSocket.setTimeout(SO_TIMEOUT);
    }

    @Override
    public SocketAdapter getCurrentUserSocket() {
        return currentUserSocket;
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

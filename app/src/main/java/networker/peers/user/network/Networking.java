package networker.peers.user.network;

import java.io.IOException;
import java.net.InetAddress;

import networker.peers.user.User;
import networker.sockets.SocketAdapter;

public interface Networking {
    void createUserSocket() throws IOException;
    void replaceSocket(SocketAdapter newSocket) throws IOException;
    boolean connectionIsUsable();
    int getPort();
    boolean hasPriority(User u);
    int getPriority();
    String getHostAddress();
    InetAddress getAddress();
    SocketAdapter getCurrentUserSocket();
    void shutdown() throws IOException;
}

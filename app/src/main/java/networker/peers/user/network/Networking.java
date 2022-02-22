package networker.peers.user.network;

import java.io.IOException;
import java.net.InetAddress;

import networker.exceptions.InvalidPortValueException;
import networker.sockets.SocketAdapter;

public interface Networking {
    void createUserSocket() throws IOException, InvalidPortValueException;
    boolean connectionIsUsable();
    void replaceSocket(SocketAdapter newSocket) throws IOException;
    SocketAdapter getCurrentUserSocket();
    boolean portIsValid();
    boolean socketIsClosed();
    int getPort();
    InetAddress getAddress();
    String getHostAddress();
    int getPriority();
    void setPriority(int p);
}

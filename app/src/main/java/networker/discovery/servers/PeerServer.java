package networker.discovery.servers;

import java.io.IOException;

import networker.sockets.ServerSocketAdapter;

public interface PeerServer {
    void listen(ServerSocketAdapter ss, final int timeToReceiveMillis, final int soTimeToReceiveMillis) throws IOException ;
}

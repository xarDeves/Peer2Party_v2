package networker.discovery.servers;

import java.io.IOException;

import networker.RoomKnowledge;
import networker.sockets.ServerSocketAdapter;

public interface PeerServer {
    void listen(ServerSocketAdapter ss, int timeToReceiveMillis, RoomKnowledge knowledge) throws IOException;
}

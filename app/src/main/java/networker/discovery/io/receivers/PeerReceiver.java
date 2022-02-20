package networker.discovery.io.receivers;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Queue;

public interface PeerReceiver {

    Queue<String> discoverPeers(DatagramSocket socket, int timeToReceiveMillis) throws IOException;
}

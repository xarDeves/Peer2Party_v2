package networker.messages.io.receivers;

import java.io.IOException;
import java.net.DatagramSocket;

public interface MessageReceiver {

    String discoverAnnouncement(DatagramSocket socket, int timeToReceiveMillis) throws IOException;
}

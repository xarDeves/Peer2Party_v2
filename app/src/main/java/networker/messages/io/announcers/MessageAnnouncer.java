package networker.messages.io.announcers;

import java.io.IOException;
import java.net.DatagramSocket;

import networker.helpers.NetworkInformation;
import networker.messages.MessageIntent;

public interface MessageAnnouncer {
    void announce(DatagramSocket socket, MessageIntent mi, NetworkInformation info) throws IOException;
}

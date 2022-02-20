package networker.discovery.io.announcers;

import java.io.IOException;
import java.net.DatagramSocket;

import networker.helpers.NetworkInformation;

public interface PeerAnnouncer {

    void announce(DatagramSocket socket, NetworkInformation info) throws IOException;
}

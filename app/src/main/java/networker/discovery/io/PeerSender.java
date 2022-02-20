package networker.discovery.io;

import java.io.IOException;
import java.net.DatagramSocket;

import networker.helpers.NetworkInformation;

public interface PeerSender {

    void announce(DatagramSocket socket, NetworkInformation info) throws IOException;
}

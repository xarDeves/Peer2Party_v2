package networker.discovery.interfaces;

import java.io.IOException;
import java.util.List;

import networker.peers.User;

public interface PeerDiscoverer {

    List<User> processOnce() throws IOException;
}

package networker.discovery.discoverers;

import java.io.IOException;

public interface PeerDiscoverer {

    void highSpeedDiscover() throws IOException;
    void discover() throws IOException;
    void listen() throws IOException;
}

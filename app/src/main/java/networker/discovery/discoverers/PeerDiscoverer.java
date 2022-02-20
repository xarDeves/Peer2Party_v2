package networker.discovery.discoverers;

import java.io.IOException;

public interface PeerDiscoverer {

    void highSpeedDiscovery() throws IOException;
    void processOnce() throws IOException;
}

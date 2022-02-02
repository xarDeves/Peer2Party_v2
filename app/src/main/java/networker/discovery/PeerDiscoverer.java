package networker.discovery;

import java.io.IOException;

public interface PeerDiscoverer {

    void processOnce() throws IOException;
}

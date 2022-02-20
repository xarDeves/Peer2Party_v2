package networker.messages.io.discoverers;

import java.io.IOException;

import networker.messages.MessageIntent;

public interface MessageDiscoverer {
    String discoverOne() throws IOException;
}

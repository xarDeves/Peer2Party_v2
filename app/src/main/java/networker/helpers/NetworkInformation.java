package networker.helpers;

import java.net.InetAddress;
import java.net.UnknownHostException;

// placeholder name, must find something more specific...
public class NetworkInformation {
    public static final String multicastMessagesAddress = "FF7E:230::1235";
    private final InetAddress multicastMessagesGroup;

    public static final String multicastDiscoveryAddress = "FF7E:230::1234";
    private final InetAddress multicastDiscoverGroup;

    private final String friendlyName;

    public NetworkInformation(String friendlyName) throws UnknownHostException {
        this.multicastDiscoverGroup = InetAddress.getByName(multicastDiscoveryAddress);
        this.multicastMessagesGroup = InetAddress.getByName(multicastMessagesAddress);
        this.friendlyName = friendlyName;
    }

    public InetAddress getMulticastMessagesGroup() {
        return multicastMessagesGroup;
    }

    public InetAddress getMulticastDiscoverGroup() {
        return multicastDiscoverGroup;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}

package networker.helpers;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import networker.exceptions.InvalidPortValueException;

// placeholder name, must find something more specific...
public class NetworkInformation {
    public static final String multicastAddressString = "FF7E:230::1234";
    private final InetAddress multicastGroup;
    private final int port;
    private final String friendlyName;

    public NetworkInformation(int port, String friendlyName) throws UnknownHostException, InvalidPortValueException {
        this.multicastGroup = Inet6Address.getByName(multicastAddressString);
        if (port < 1025 || port > 49151) throw new InvalidPortValueException();
        this.port = port;
        this.friendlyName = friendlyName;
    }

    public InetAddress getMulticastGroup() {
        return multicastGroup;
    }

    public int getPort() {
        return port;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}

package networker.helpers;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

// placeholder name, must find something more specific...
public class NetworkInformation {
    public static final String multicastMessagesAddressv6 = "FF7E:230::1235";
    public static final String multicastMessagesAddressv4 = "230.0.0.1";
    private final InetAddress multicastMessagesGroup;

    public static final String multicastDiscoveryAddressv6 = "FF7E:230::1234";
    public static final String multicastDiscoveryAddressv4 = "230.0.0.1";
    private final InetAddress multicastDiscoverGroup;

    private final String friendlyName;

    public NetworkInformation(String friendlyName) throws UnknownHostException {
        InetAddress multicastMessagesGroupTemp;
        InetAddress multicastDiscoverGroupTemp;
        try {
            multicastDiscoverGroupTemp = InetAddress.getByName(multicastDiscoveryAddressv6);
            multicastMessagesGroupTemp = InetAddress.getByName(multicastMessagesAddressv6);
        } catch (UnknownHostException e) {
            Log.d("networker", "v6 failed", e);

            try {
                multicastDiscoverGroupTemp = InetAddress.getByName(multicastDiscoveryAddressv4);
                multicastMessagesGroupTemp = InetAddress.getByName(multicastMessagesAddressv4);
            } catch (UnknownHostException ex) {
                Log.d("networker", "v4 failed", ex);

                throw ex;
            }
        }

        multicastMessagesGroup = multicastMessagesGroupTemp;
        multicastDiscoverGroup = multicastDiscoverGroupTemp;
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

package networker.helpers;

import java.net.NetworkInterface;

public enum NetworkType {
    MAIN_INTERFACE, ADHOC, UNKNOWN;

    public static boolean IS_MAIN_INTERFACE(NetworkInterface iface) {
        return iface.getDisplayName().contains("wlan");
    }

    public static boolean IS_ADHOC_HOTSPOT(NetworkInterface iface) {
        return iface.getDisplayName().contains("swlan");
    }

    public static NetworkType getIfaceType(NetworkInterface iface) {
        if (IS_MAIN_INTERFACE(iface)) return MAIN_INTERFACE;
        if (IS_ADHOC_HOTSPOT(iface)) return ADHOC;

        return UNKNOWN;
    }

}

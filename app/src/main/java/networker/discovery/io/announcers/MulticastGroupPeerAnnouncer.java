package networker.discovery.io.announcers;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import networker.helpers.NetworkInformation;
import networker.helpers.NetworkUtilities;
import networker.peers.User;

/** Requires multicast permissions.
 * Requires acquiring WifiManager.MulticastLock.
 * Even then, depending on the device this might not work (it will either fail silently,
 * or with a bang depending on the device, we cannot know).
 * source:
 * @link https://codeisland.org/2012/udp-multicast-on-android
 */
public class MulticastGroupPeerAnnouncer implements PeerAnnouncer {
    // this must later run as a client (send only when prompted)
    // this must have the input of ourself (the current user)
    private final User ourself;

    public MulticastGroupPeerAnnouncer(User user) {
        ourself = user;
    }

    public void announce(DatagramSocket socket, NetworkInformation info) throws IOException {
        // https://developer.android.com/reference/java/net/MulticastSocket
        // we don't need to be a member of the multicast group to send messages to it, so no real processing is required here

        try {
            String s = NetworkUtilities.getUserSalutationJSON(ourself).toString();
            byte[] salutations = NetworkUtilities.convertUTF8StringToBytes(s);
            DatagramPacket msg = NetworkUtilities.createDatagramPacket(salutations, info.getMulticastDiscoverGroup(), info.getDiscoverPort());
            // double send
            socket.send(msg);
            Log.d("networker.discovery.io.announcers.announce", "Sent first, bLength " + salutations.length + " " + s);
            socket.send(msg);
            Log.d("networker.discovery.io.announcers.announce", "Sent second, bLength " + salutations.length + " " + s);
        } catch (JSONException e) {
            Log.d("networker.discovery.io.announcers.announce", "NetworkUtilities.getUserSalutation(ourself)", e);
        }
    }

}

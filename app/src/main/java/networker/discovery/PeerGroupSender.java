package networker.discovery;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

import networker.helpers.NetworkInformation;
import networker.helpers.NetworkUtilities;
import networker.User;

class PeerGroupSender {
    // this must later run as a client (send only when prompted)
    // this must have the input of ourself (the current user)
    private final User ourself;

    PeerGroupSender(User user) {
        ourself = user;
    }

    void multicastOurself(MulticastSocket socket, NetworkInformation info) throws IOException {
        // https://developer.android.com/reference/java/net/MulticastSocket
        // we don't need to be a member of the multicast group to send messages to it, so no real processing is required here

        try {
            String s = NetworkUtilities.getUserSalutation(ourself);
            byte[] salutations = NetworkUtilities.convertUTF8StringToBytes(s);
            DatagramPacket msg = NetworkUtilities.createDatagramPacket(salutations, info.getMulticastGroup(), info.getPort());
            // double send
            socket.send(msg);
            Log.d("networker", "Sent first, bLength " + salutations.length + " " + s);
            socket.send(msg);
            Log.d("networker", "Sent second, bLength " + salutations.length + " " + s);
        } catch (JSONException e) {
            Log.d("networker", "NetworkUtilities.getUserSalutation(ourself)", e);
        }
    }

}

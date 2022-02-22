package networker.messages.io.announcers;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import networker.helpers.NetworkInformation;
import networker.helpers.NetworkUtilities;
import networker.messages.MessageIntent;

public class MulticastGroupMessageAnnouncer implements MessageAnnouncer {
    private static final String TAG = "networker.messages.io.announcers:MulticastGroupMessageAnnouncer";

    @Override
    public void announce(DatagramSocket socket, MessageIntent mi, NetworkInformation info) throws IOException {
        try {
            String js = NetworkUtilities.createMessageIntentJSON(mi);
            byte[] decl = NetworkUtilities.convertUTF8StringToBytes(js);
            DatagramPacket msg = NetworkUtilities.createDatagramPacket(decl, info.getMulticastMessagesGroup(), info.getMessagePort());
            socket.send(msg);
            Log.d(TAG + ".announce", "Sent bLength " + decl.length + " " + js);
        } catch (JSONException e) {
            Log.e(TAG + ".announce", "NetworkUtilities.getDeclarationBroadcast(mi);", e);
        }
    }
}

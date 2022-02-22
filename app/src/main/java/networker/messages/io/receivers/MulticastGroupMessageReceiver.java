package networker.messages.io.receivers;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

import networker.helpers.NetworkUtilities;

public class MulticastGroupMessageReceiver implements MessageReceiver {
    private static final String TAG = "networker.messages.io.receivers:MulticastGroupMessageReceiver";

    @Override
    public String discoverAnnouncement(DatagramSocket so, int timeToReceiveMillis) throws IOException {
        // https://developer.android.com/reference/java/net/MulticastSocket
        // we must have joined the group (with the current socket) in order to receive messages
        // timeout must also be set BEFORE receiving...
        MulticastSocket socket = (MulticastSocket) so;

        long timeSpent = 0;
        final long start = System.currentTimeMillis();

        while (timeSpent < timeToReceiveMillis) {
            try {
                return receivePeerMulticast(socket);
            } catch (SocketTimeoutException e) {
                Log.v(TAG + ".discoverAnnouncement", "", e);
            }

            long end = System.currentTimeMillis();
            timeSpent = end - start;

            Log.d(TAG + ".discoverAnnouncement", "current time spent " + timeSpent);
        }

        return "";
    }

    private String receivePeerMulticast(MulticastSocket socket) throws IOException {
        byte[] buffer = new byte[NetworkUtilities.MAX_MESSAGE_DECLARATION_BUFFER_SIZE];
        DatagramPacket recv = new DatagramPacket(buffer, buffer.length);
        socket.receive(recv);
        Log.d(TAG + ".receivePeerMulticast", "recv.getLength() " + recv.getLength());
        return NetworkUtilities.convertBytesToUTF8String(buffer);
    }
}

package networker.messages.io.receivers;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

import networker.helpers.NetworkUtilities;

public class MulticastGroupMessageReceiver implements MessageReceiver {

    @Override
    public String discoverAnnouncement(DatagramSocket so, int timeToReceiveMillis) throws IOException {
        // https://developer.android.com/reference/java/net/MulticastSocket
        // we must have joined the group (with the current socket) in order to receive messages
        // timeout must also be set BEFORE receiving...
        MulticastSocket socket = (MulticastSocket) so;

        long timeSpent = 0;
        final long start = System.currentTimeMillis();

        while (timeSpent < timeToReceiveMillis) {
            String s = "";

            try {
                return receivePeerMulticast(socket);
            } catch (SocketTimeoutException e) {
                Log.d("MulticastGroupMessageReceiver.discoverAnnouncement", "SocketTimeoutException", e);
            }

            long end = System.currentTimeMillis();
            timeSpent = end - start;

            Log.d("MulticastGroupMessageReceiver.discoverAnnouncement", "current time spent " + timeSpent + " found data " + s);
        }

        return "";
    }

    private String receivePeerMulticast(MulticastSocket socket) throws IOException {
        byte[] buffer = new byte[NetworkUtilities.MAX_MESSAGE_DECLARATION_BUFFER_SIZE];
        DatagramPacket recv = new DatagramPacket(buffer, buffer.length);
        socket.receive(recv);
        Log.d("MulticastGroupMessageReceiver.receivePeerMulticast", "recv.getLength() " + recv.getLength());
        return NetworkUtilities.convertBytesToUTF8String(buffer);
    }
}

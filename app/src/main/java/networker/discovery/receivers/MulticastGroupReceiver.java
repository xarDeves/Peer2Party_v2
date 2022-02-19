package networker.discovery.receivers;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;

import networker.discovery.PeerReceiver;
import networker.helpers.NetworkUtilities;

/**
 * Requires multicast permissions.
 * Requires acquiring WifiManager.MulticastLock.
 * Even then, depending on the device this might not work (it will either fail silently,
 * or with a bang depending on the device, we cannot know).
 * source:
 *
 * @link https://codeisland.org/2012/udp-multicast-on-android
 */
public class MulticastGroupReceiver implements PeerReceiver {
    // this must run as a server with a timed delay (15s receive, with a random +/- of 1s, then send twice)

    public Queue<String> discoverPeers(DatagramSocket socket, final int timeToReceiveMillis) throws IOException {
        // https://developer.android.com/reference/java/net/MulticastSocket
        // we must have joined the group (with the current socket) in order to receive messages
        // timeout must also be set BEFORE receiving...

        Queue<String> dataFound = new LinkedList<>();

        long timeSpent = 0;
        final long start = System.currentTimeMillis();

        while (timeSpent < timeToReceiveMillis) {
            String s = "";

            try {
                dataFound.add(receivePeerMulticast(socket));
            } catch (SocketTimeoutException e) {
                Log.d("networker", "SocketTimeoutException", e);
            }

            long end = System.currentTimeMillis();
            timeSpent = end - start;

            Log.d("networker", "current time spent " + timeSpent + " found data " + s);

        }

        return dataFound;
    }

    private String receivePeerMulticast(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[NetworkUtilities.DISCOVERY_BUFFER_SIZE];
        DatagramPacket recv = new DatagramPacket(buffer, buffer.length);
        socket.receive(recv);
        return NetworkUtilities.convertBytesToUTF8String(buffer);
    }
}
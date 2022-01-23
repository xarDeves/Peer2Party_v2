package networker.discovery;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;

import networker.discovery.interfaces.PeerReceiver;
import networker.helpers.NetworkUtilities;

public class MulticastGroupReceiver implements PeerReceiver {
    // this must run as a server with a timed delay (15s receive, with a random +/- of 1s, then send twice)

    public Queue<String> discoverPeers(DatagramSocket socket, int timeToReceiveMillis) throws IOException {
        // https://developer.android.com/reference/java/net/MulticastSocket
        // we must have joined the group (with the current socket) in order to receive messages
        // timeout must also be set BEFORE receiving...

        Queue<String> dataFound = new LinkedList<>();

        try {
            long timeSpent = 0;
            long start = System.currentTimeMillis();

            while (timeSpent < timeToReceiveMillis) {

                byte[] buffer = new byte[NetworkUtilities.DISCOVERY_BUFFER_SIZE];
                DatagramPacket recv = new DatagramPacket(buffer, buffer.length);
                socket.receive(recv);

                long end = System.currentTimeMillis();

                String s = NetworkUtilities.convertBytesToUTF8String(buffer);
                dataFound.add(s);

                timeSpent = end - start;

                Log.d("networker", "current time spent " + timeSpent + " found data " + s);
            }

        } catch (SocketTimeoutException e) {
            Log.d("networker", "SocketTimeoutException", e);
        }

        return dataFound;
    }
}

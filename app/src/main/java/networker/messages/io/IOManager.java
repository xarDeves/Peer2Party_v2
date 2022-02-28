package networker.messages.io;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import networker.RoomKnowledge;
import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkInformation;
import networker.helpers.NetworkUtilities;
import networker.messages.MessageIntent;
import networker.messages.io.announcers.MessageAnnouncer;
import networker.messages.io.processors.inbound.InboundMessageProcessor;
import networker.messages.io.processors.outbound.OutboundMessageProcessor;
import networker.messages.io.receivers.MessageReceiver;

public class IOManager implements MessageManager {
    private static final String TAG = "networker.messages.io:IOManager";

    public static final int TEXT_BLOCK_SIZE = 10_000; // 10 KiB
    public static final int MULTIMEDIA_BLOCK_SIZE = 5_000_000; // 5 MiB

    public static final int MAXIMUM_TEXT_SIZE = 50_000_000; // 50 MiB
    public static final long MAXIMUM_MULTIMEDIA_SIZE = 1_000_000_000; // 1 GiB

    public static final int BO_TIMEOUT_MILLIS = 5000;

    private final MessageReceiver discoverer;
    private final OutboundMessageProcessor outboundProcessor;
    private final InboundMessageProcessor inboundProcessor;
    private final NetworkInformation netInfo;
    private final MessageAnnouncer announcer;
    private final DatagramSocket udpSocket;
    private final RoomKnowledge rk;

    public IOManager(MessageReceiver mr, MessageAnnouncer ma, DatagramSocket ds,
                     OutboundMessageProcessor omp, InboundMessageProcessor imp,
                     RoomKnowledge room, NetworkInformation info) {

        discoverer = mr;
        announcer = ma;
        udpSocket = ds;
        rk = room;

        outboundProcessor = omp;
        inboundProcessor = imp;
        netInfo = info;
    }

    /** This method is blocking, and should be run in a thread in a loop. */
    @Override
    public void discover() throws IOException {
        try {
            MessageIntent mi;
            do {
                String intentJson = discoverer.discoverAnnouncement(udpSocket, BO_TIMEOUT_MILLIS);
                mi = NetworkUtilities.processMessageIntent(intentJson);
            } while (netInfo.isOurself(mi.getSource())); //making sure we're not receiving ourself

            // if roomknowledge has the peer that sent this message, and he's enabled on our side
            if (rk.hasPeer(mi.getSource()) && rk.getPeer(mi.getSource()).isEnabled()) {
                if (containsOurself(mi)) receive(mi);
            }
        } catch (JSONException | InvalidPortValueException | UnknownHostException e) {
            Log.e(TAG + ".discover", "", e);
        }
    }

    private boolean containsOurself(MessageIntent mi) {
        for (String r: mi.getReceivers()) {
            //we're one of the receivers, receive this intent
            if (r.equals(netInfo.getOurselves().getNetworking().getHostAddress())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void receive(MessageIntent mi) {
        inboundProcessor.receive(mi);
    }

    @Override
    public void send(MessageIntent mi) throws IOException {
        outboundProcessor.send(mi);
        announcer.announce(udpSocket, mi, netInfo);
    }

}

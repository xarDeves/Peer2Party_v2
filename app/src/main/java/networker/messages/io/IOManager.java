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
import networker.peers.User;

public class IOManager implements MessageManager {
    public static final int MULTIMEDIA_BLOCK_SIZE = 500_000; // 500 KB
    public static final int TEXT_BLOCK_SIZE = 10_000; // 10 KB
    public static final int MAXIMUM_TEXT_SIZE = 10_000_000; // 10 MB
    public static final long MAXIMUM_MULTIMEDIA_SIZE = 1_000_000_000; // 1 GB

    public static final int BO_TIMEOUT_MILLIS = 5000;

    private final MessageReceiver discoverer;
    private final OutboundMessageProcessor outboundProcessor;
    private final InboundMessageProcessor inboundProcessor;
    private final NetworkInformation netInfo;
    private final User ourself;
    private final MessageAnnouncer announcer;
    private final DatagramSocket udpSocket;
    private final RoomKnowledge rk;

    public IOManager(MessageReceiver mr, MessageAnnouncer ma, DatagramSocket ds,
                     OutboundMessageProcessor omp, InboundMessageProcessor imp,
                     RoomKnowledge room, NetworkInformation info, User ourself) {

        discoverer = mr;
        announcer = ma;
        udpSocket = ds;
        rk = room;

        outboundProcessor = omp;
        inboundProcessor = imp;
        netInfo = info;
        this.ourself = ourself;
    }

    /** This method is blocking, and should be run in a thread in a loop. */
    @Override
    public void discover() throws IOException {
        try {
            String intentJson = discoverer.discoverAnnouncement(udpSocket, BO_TIMEOUT_MILLIS);
            MessageIntent mi = NetworkUtilities.processMessageIntent(intentJson);

            // if roomknowledge has the peer, and he's enabled on our side, check if we're one of the receivers
            if (rk.hasPeer(mi.getSource()) && rk.getPeer(mi.getSource()).isEnabled()) {
                for (String r: mi.getReceivers()) {
                    //we're one of the receivers, receive this intent
                    if (r.equals(ourself.getIDENTIFIER())) {
                        receive(mi);
                        break;
                    }
                }
            }
        } catch (JSONException | InvalidPortValueException | UnknownHostException e) {
            Log.d("networker.messages.io.discover" , "NetworkUtilities.processMessageIntent(intentJson);", e);
        }
    }

    @Override
    public void receive(MessageIntent mi) {
        inboundProcessor.receive(mi);
    }

    @Override
    public void send(MessageIntent mi) throws IOException {
        synchronized (this) {
            announcer.announce(udpSocket, mi, netInfo);
            outboundProcessor.send(mi);
        }
    }

}

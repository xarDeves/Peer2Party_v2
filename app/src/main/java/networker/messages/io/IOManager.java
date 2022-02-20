package networker.messages.io;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.net.UnknownHostException;

import networker.RoomKnowledge;
import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkUtilities;
import networker.messages.MessageIntent;
import networker.messages.io.discoverers.MessageDiscoverer;
import networker.messages.io.processors.InboundMessageProcessor;
import networker.messages.io.processors.OutboundMessageProcessor;

public class IOManager implements MessageManager {
    public static final int MULTIMEDIA_BLOCK_SIZE = 500_000; // 500 KB
    public static final int TEXT_BLOCK_SIZE = 10_000; // 10 KB

    private final MessageDiscoverer discoverer;
    private final OutboundMessageProcessor outboundProcessor;
    private final InboundMessageProcessor inboundProcessor;
    private final RoomKnowledge rk;

    public IOManager(MessageDiscoverer md,
                     OutboundMessageProcessor omp, InboundMessageProcessor imp,
                     RoomKnowledge room) {
        // private final ExecutorService inboundExecutor = Executors.newFixedThreadPool(executorTCount); FOR IMP
        // private final ExecutorService outboundExecutor = Executors.newFixedThreadPool(executorTCount); FOR OMP

        discoverer = md;
        rk = room;

        outboundProcessor = omp;
        inboundProcessor = imp;
    }

    /**
     * This method is blocking, and should be run in a thread in a while loop
     */
    @Override
    public void discover() throws IOException {
        MessageIntent mi = null;
        try {
            String intentJson = discoverer.discoverOne();
            mi = NetworkUtilities.processMessageIntent(intentJson);
        } catch (JSONException | InvalidPortValueException | UnknownHostException e) {
            Log.d("networker" , "NetworkUtilities.processMessageIntent(intentJson);", e);
        }

        if (mi != null) {
            if (rk.hasPeer(mi.getSource())) receive(mi);
        }
    }

    @Override
    public void receive(MessageIntent mi) {
        inboundProcessor.receive(mi);
    }

    @Override
    public void send(MessageIntent mi) {
        outboundProcessor.send(mi);
    }

}

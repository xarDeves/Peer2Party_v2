package networker.messages.io.processors;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;

import helpers.DatabaseBridge;
import networker.RoomKnowledge;
import networker.messages.MessageIntent;

public class InboundProcessor implements InboundMessageProcessor {
    private final ExecutorService executor;
    private final RoomKnowledge rk;
    private final DatabaseBridge dbb;

    public InboundProcessor(@NonNull ExecutorService executorService,
                            @NonNull RoomKnowledge roomKnowledge,
                            @NonNull DatabaseBridge databaseBridge) {

        executor = executorService;
        rk = roomKnowledge;
        dbb = databaseBridge;
    }

    @Override
    public void receive(MessageIntent mi) {

        //TODO sum data received to rk
    }
}

package networker.messages.io.processors.inbound;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import helpers.DatabaseBridge;
import networker.RoomKnowledge;
import networker.messages.MessageDeclaration;
import networker.messages.MessageIntent;
import networker.messages.io.handlers.InboundHandler;

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
    public void receive(@NonNull MessageIntent mi) {
        executor.execute(() -> {
            Iterator<MessageDeclaration> mdls = mi.getMessageDeclarations();
            for (MessageDeclaration mdl = mdls.next(); mdls.hasNext(); mdl = mdls.next()) {
                (new InboundHandler(mdl, rk.getPeer(mi.getSource()).getUser(), rk, dbb)).handle();
            }
        });
    }
}

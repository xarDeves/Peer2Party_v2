package networker.messages.io.processors;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import helpers.DatabaseBridge;
import networker.RoomKnowledge;
import networker.messages.io.handlers.OutboundHandler;
import networker.messages.MessageDeclaration;
import networker.messages.MessageIntent;
import networker.messages.content.ContentProcurer;
import networker.messages.content.factories.ContentProcurerFactory;
import networker.peers.Peer;
import networker.peers.User;

public class OutboundProcessor implements OutboundMessageProcessor {
    private final ExecutorService executor;
    private final RoomKnowledge rk;
    private final DatabaseBridge dbb;

    public OutboundProcessor(@NonNull ExecutorService executorService,
                             @NonNull RoomKnowledge roomKnowledge,
                             @NonNull DatabaseBridge databaseBridge) {
        executor = executorService;
        rk = roomKnowledge;
        dbb = databaseBridge;
    }

    @Override
    public void send(@NonNull MessageIntent intent) {
        for (String r : intent.getReceivers()) {
            Peer p = rk.getPeer(r);
            //if the peer exists, and is enabled
            if (p != null) {
                dispatchThread(p.getUser(), intent.getMessageDeclarations());
            }
        }
        Iterator<MessageDeclaration> mdls = intent.getMessageDeclarations();
        for (MessageDeclaration mdl = mdls.next(); mdls.hasNext(); mdl = mdls.next()) {
            if (mdl.getContentType().isFile()) { dbb.onMultimediaSend(mdl); break; }
            else { dbb.onTextSend(mdl); }
        }
    }

    private void dispatchThread(final User u, final Iterator<MessageDeclaration> mdls) {
        executor.execute(() -> {
            for (MessageDeclaration mdl = mdls.next(); mdls.hasNext(); mdl = mdls.next()) {
                try {
                    ContentProcurer cpr = ContentProcurerFactory.createProcurer(mdl);
                    (new OutboundHandler(u, cpr, rk)).handle();
                } catch (IOException e) {
                    Log.d("outboundProcessor.dispatchThread", e.getMessage(), e);
                }
            }
        });
    }

}

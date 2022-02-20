package networker.messages.io.processors.outbound;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import helpers.DatabaseBridge;
import networker.RoomKnowledge;
import networker.messages.MessageDeclaration;
import networker.messages.MessageIntent;
import networker.messages.content.ContentProcurer;
import networker.messages.content.factories.ContentProcurerFactory;
import networker.messages.io.handlers.OutboundHandler;
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
        List<MessageDeclaration> mdls = copyMessageDeclarations(intent.getMessageDeclarations());

        for (String r : intent.getReceivers()) {
            Peer p = rk.getPeer(r);
            //if the peer exists, and is enabled
            if (p != null) {
                dispatchThread(p.getUser(), mdls);
            }
        }

        for (MessageDeclaration md: mdls) {
            if (md.getContentType().isFile()) {
                dbb.onMultimediaSend(md);
            } else {
                dbb.onTextSend(md);
            }
        }
    }

    private List<MessageDeclaration> copyMessageDeclarations(final Iterator<MessageDeclaration> mdlIterator) {
        LinkedList<MessageDeclaration> mdls = new LinkedList<>();
        for (MessageDeclaration mdl = mdlIterator.next(); mdlIterator.hasNext(); mdl = mdlIterator.next()) {
            mdls.add(mdl);
        }
        return mdls;
    }

    private void dispatchThread(final User u, final List<MessageDeclaration> mdls) {
        executor.execute(() -> {
            for (MessageDeclaration md: mdls) {
                try {
                    ContentProcurer cpr = ContentProcurerFactory.createProcurer(md);
                    (new OutboundHandler(u, cpr, rk)).handle();
                } catch (IOException e) {
                    Log.d("outboundProcessor.dispatchThread", e.getMessage(), e);
                }
            }
        });
    }

}

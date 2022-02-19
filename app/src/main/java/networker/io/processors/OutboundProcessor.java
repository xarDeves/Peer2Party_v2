package networker.io.processors;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import networker.RoomKnowledge;
import networker.io.handlers.OutboundHandler;
import networker.messages.MessageDeclaration;
import networker.messages.MessageIntent;
import networker.messages.content.ContentProcurer;
import networker.messages.content.factories.ContentProcurerFactory;
import networker.peers.Peer;
import networker.peers.User;

public class OutboundProcessor {
    private final ExecutorService executor;
    private final RoomKnowledge roomKnowledge;

    public OutboundProcessor(@NonNull ExecutorService executorService, RoomKnowledge rk) {
        executor = executorService;
        roomKnowledge = rk;
    }

    public void execute(MessageIntent intent) {
        for (String r : intent.getReceivers()) {
            Peer p = roomKnowledge.getPeer(r);
            if (p != null) dispatchThread(p.getUser(), intent.getMessageDeclarations());
        }
    }

    private void dispatchThread(User u, Iterator<MessageDeclaration> mdls) {
        executor.execute(() -> {
            for (MessageDeclaration mdl = mdls.next(); mdls.hasNext(); mdl = mdls.next()) {
                try (ContentProcurer cpr = ContentProcurerFactory.createProcurer(mdl)) {
                    (new OutboundHandler(u, cpr)).handle();
                } catch (IOException e) {
                    Log.d("outboundProcessor", e.getMessage(), e);
                }
            }
        });
    }

}

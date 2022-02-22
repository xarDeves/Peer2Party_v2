package networker.messages.io.processors.outbound;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import helpers.db.DatabaseBridge;
import networker.RoomKnowledge;
import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkInformation;
import networker.helpers.NetworkUtilities;
import networker.messages.MessageDeclaration;
import networker.messages.MessageIntent;
import networker.messages.content.ContentProcurer;
import networker.messages.content.factories.ContentProcurerFactory;
import networker.messages.io.handlers.OutboundHandler;
import networker.peers.Peer;
import networker.peers.user.User;

public class OutboundProcessor implements OutboundMessageProcessor {
    private static final String TAG = "networker.messages.io.processors.outbound:OutboundProcessor";

    private final ExecutorService executor;
    private final NetworkInformation netInfo;
    private final RoomKnowledge rk;
    private final DatabaseBridge dbb;

    public OutboundProcessor(@NonNull ExecutorService executorService,
                             @NonNull NetworkInformation networkInformation,
                             @NonNull RoomKnowledge roomKnowledge,
                             @NonNull DatabaseBridge databaseBridge) {
        executor = executorService;
        netInfo = networkInformation;
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

        Iterator<MessageDeclaration> mdlIterator = intent.getMessageDeclarations();
        while (mdlIterator.hasNext()) {
            MessageDeclaration md = mdlIterator.next();
            if (md.getContentType().isFile()) {
                dbb.onMultimediaSend(md);
            } else {
                dbb.onTextSend(md);
            }
        }
    }

    private void dispatchThread(final User u, final Iterator<MessageDeclaration> mdls) {
        executor.execute(() -> {
            while(mdls.hasNext()) {
                MessageDeclaration md = mdls.next();
                try {
                    ContentProcurer cpr = ContentProcurerFactory.createProcurer(md);

                    try {
                        NetworkUtilities.createConnectionIfThereIsNone(u, netInfo);
                    } catch (IOException e) {
                        Log.e(TAG + ".dispatchThread", "Shutdown failed when IOException is thrown?", e);
                        u.getNetworking().shutdown();
                        return;
                    } catch (InterruptedException | InvalidPortValueException | JSONException e) {
                        Log.e(TAG + ".dispatchThread", "", e);
                        return;
                    }

                    (new OutboundHandler(u, cpr, rk)).handle();
                } catch (IOException e) {
                    Log.e(TAG + ".dispatchThread", "", e);
                    try {
                        u.getNetworking().shutdown();
                    } catch (IOException ioException) {
                        Log.e(TAG + ".dispatchThread", "Shutdown failed when IOException is thrown?", ioException);
                    }
                }
            }
        });
    }

}

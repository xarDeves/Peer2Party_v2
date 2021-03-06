package networker.messages.io.processors.outbound;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
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
    public void send(@NonNull final MessageIntent intent) {
        Queue<User> q = filterReceivers(intent);
        for (User u:q) {
            dispatchThread(u, intent.getMessageDeclarations());
        }

        Iterator<MessageDeclaration> mdlIterator = intent.getMessageDeclarations();
        addOutToDB(mdlIterator);
    }

    private Queue<User> filterReceivers(@NonNull final MessageIntent intent) {
        Queue<User> q = new LinkedList<>();
        for (String r : intent.getReceivers()) {
            Peer p = rk.getPeer(r);
            //if the peer exists
            if (p != null) {
                q.add(p.getUser());
            }
        }

        return q;
    }

    private void addOutToDB(Iterator<MessageDeclaration> mdlIterator) {
        while (mdlIterator.hasNext()) {
            MessageDeclaration md = mdlIterator.next();
            if (md.getContentType().isFile()) {
                dbb.onMultimediaSend(md);
            } else {
                dbb.onTextSend(md);
            }
        }
    }

    /** Sends the data 1:N, 1 user, N declarations */
    private void dispatchThread(final User u, final Iterator<MessageDeclaration> mdls) {
        executor.execute(() -> {
            if (!createConnection(u)) {
                // ABORT!
                Log.e(TAG + ".dispatchThread", "Fatal failure! There was no socket, or creation of new socket failed fatally!");
                return;
            }
            while(mdls.hasNext()) {
                MessageDeclaration md = mdls.next();
                try {
                    ContentProcurer cpr = ContentProcurerFactory.createProcurer(md);
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

    /** Returns true if everything's okay, false to abort. This spaghetti is, unfortunately, unavoidable. */
    private boolean createConnection(final User u) {
        try {
            u.getSynchronization().lock();
            NetworkUtilities.createConnectionIfThereIsNone(u, netInfo.getOurselves());
        } catch (SocketTimeoutException e) {
            //should we do something particular in this situation?
            Log.d(TAG + ".createConnection", "", e);
            return false;
        } catch (InterruptedException | InvalidPortValueException | JSONException e) {
            Log.e(TAG + ".createConnection", "", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG + ".createConnection", "Shutdown failed when IOException is thrown?", e);
            try {
                u.getNetworking().shutdown();
            } catch (IOException ioException) {
                Log.e(TAG + ".createConnection", "Shutdown failed when IOException is thrown?", ioException);
            }
            return false;
        } finally {
            u.getSynchronization().unlock();
        }
        return true;
    }

}

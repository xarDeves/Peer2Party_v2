package networker.messages.io.handlers;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

import networker.RoomKnowledge;
import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkUtilities;
import networker.messages.content.ContentProcurer;
import networker.peers.user.User;
import networker.peers.user.synchronization.Synchronization;

public class OutboundHandler {
    private static final String TAG = "networker.messages.io.handlers:OutboundHandler";

    private final User user;
    private final ContentProcurer procurer;
    private final RoomKnowledge rk;

    public OutboundHandler(User u, ContentProcurer cpr, RoomKnowledge rk) {
        user = u;
        procurer = cpr;
        this.rk = rk;
    }

    public void handle() {
        Synchronization sync = user.getSynchronization();
        try {
            sync.sendLock();

            try {
                NetworkUtilities.createConnectionIfThereIsNone(user);
            } catch (IOException | InterruptedException | InvalidPortValueException e) {
                Log.e(TAG + ".handle", "couldn't createConnectionIfThereIsNone", e);
                return;
            }

            DataOutputStream dos = user.getNetworking().getCurrentUserSocket().getDataOutputStream();
            sendHeader(dos);
            sendBody(dos);

            rk.increaseContentSizeSent(procurer.getTotalSize());
            rk.incrementMessagesSent();
            Log.d(TAG + ".handle", "Successfully sent to " + user.getUsername());
        } catch (InterruptedException e) {
            Log.e(TAG + ".handle", "InterruptedException when sending to " + user.getUsername(), e);
        } catch (IOException e) {
            Log.e(TAG + ".handle", "IOException when sending to " + user.getUsername(), e);
        } finally {
            sync.sendUnlock();
        }
    }

    private void sendHeader(DataOutputStream dos) throws IOException {
        while (procurer.hasNextHeader()) {
            byte[] piece = procurer.getHeaderPiece();
            dos.write(piece, 0, piece.length);
            dos.flush();
        }
    }

    private void sendBody(DataOutputStream dos) throws IOException {
        while (procurer.hasNextContent()) {
            int bRead = procurer.consume();
            byte[] piece = procurer.getContentPiece();
            dos.write(piece, 0, bRead);
            dos.flush();
        }
    }
}

package networker.messages.io.handlers;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

import networker.RoomKnowledge;
import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkUtilities;
import networker.messages.content.ContentProcurer;
import networker.peers.User;

public class OutboundHandler {
    private final User user;
    private final ContentProcurer procurer;
    private final RoomKnowledge rk;

    public OutboundHandler(User u, ContentProcurer cpr, RoomKnowledge rk) {
        user = u;
        procurer = cpr;
        this.rk = rk;
    }

    public void handle() {
        try {
            user.sendLock();

            try {
                NetworkUtilities.createConnectionIfThereIsNone(user);
            } catch (IOException | InterruptedException | InvalidPortValueException e) {
                Log.d("networker.messages.io.handlers.handle", "couldn't createConnectionIfThereIsNone, returning", e);
                return;
            }

            DataOutputStream dos = user.getCurrentUserSocket().getDataOutputStream();
            sendHeader(dos);
            sendBody(dos);

            rk.increaseContentSizeSent(procurer.getTotalSize());
            rk.incrementMessagesSent();
            Log.d("networker.messages.io.handlers.handle", "Successfully sent to " + user.getIDENTIFIER());
        } catch (InterruptedException e) {
            Log.d("networker.messages.io.handlers.handle", "OutboundHandler interrupted uid " + user.getIDENTIFIER(), e);
        } catch (IOException e) {
            Log.d("networker.messages.io.handlers.handle", "OutboundHandler failed uid " + user.getIDENTIFIER(), e);
        } finally {
            user.sendUnlock();
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

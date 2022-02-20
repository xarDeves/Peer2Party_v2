package networker.messages.io.handlers;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

import networker.RoomKnowledge;
import networker.exceptions.InvalidPortValueException;
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
            createConnectionIfThereIsNone();
        } catch (IOException | InterruptedException | InvalidPortValueException e) {
            Log.d("networker", "couldn't createConnectionIfThereIsNone", e);
        }

        try {
            user.lock();
            DataOutputStream dos = user.getCurrentUserSocket().getDataOutputStream();
            sendHeader(dos);
            sendBody(dos);
            user.unlock();

            procurer.close();

            rk.increaseContentSizeSent(procurer.getTotalSize());
            rk.incrementMessagesSent();
            Log.d("networker", "Successfully sent to " + user.getIDENTIFIER());
        } catch (InterruptedException | IOException e) {
            Log.d("networker", "OutboundHandler failed uid " + user.getIDENTIFIER(), e);
        }
    }

    private void createConnectionIfThereIsNone() throws IOException, InterruptedException, InvalidPortValueException {
        if (user.isUsable()) return;

        user.createUserSocket();
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
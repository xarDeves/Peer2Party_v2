package networker.messages.io.handlers;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

import networker.exceptions.InvalidPortValueException;
import networker.messages.content.ContentProcurer;
import networker.peers.User;

public class OutboundHandler {
    private final User user;
    private final ContentProcurer procurer;

    public OutboundHandler(User u, ContentProcurer cpr) {
        user = u;
        procurer = cpr;
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
        } catch (InterruptedException | IOException e) {
            Log.d("networker", "OutboundHandler failed to send to " + user.getIDENTIFIER(), e);
        }
    }

    private void createConnectionIfThereIsNone() throws IOException, InterruptedException, InvalidPortValueException {
        if (user.isUsable()) return;

        user.createUserSocket();
    }

    private void sendHeader(DataOutputStream dos) throws IOException {
        while (procurer.hasNextHeader()) {
            byte[] piece = procurer.getHeaderPiece();
            dos.write(piece);
        }
    }

    private void sendBody(DataOutputStream dos) throws IOException {
        while (procurer.hasNextContent()) {
            byte[] piece = procurer.getContentPiece();
            dos.write(piece);
        }
    }
}

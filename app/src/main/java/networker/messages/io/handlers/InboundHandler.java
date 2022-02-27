package networker.messages.io.handlers;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

import helpers.db.DatabaseBridge;
import networker.RoomKnowledge;
import networker.exceptions.OversizedMessage;
import networker.exceptions.OversizedMultimediaMessage;
import networker.exceptions.OversizedTextMessage;
import networker.messages.MessageDeclaration;
import networker.messages.content.providers.MultimediaProvider;
import networker.messages.content.providers.TextProvider;
import networker.messages.io.IOManager;
import networker.peers.user.User;
import networker.peers.user.synchronization.Synchronization;

public class InboundHandler {
    private static final String TAG = "networker.messages.io.handlers:InboundHandler";

    private final MessageDeclaration mdl;
    private final User u;
    private final RoomKnowledge rk;
    private final DatabaseBridge dbb;

    //TODO add mediastore/disk API facade
    public InboundHandler(MessageDeclaration messageDeclaration, User user,
                          RoomKnowledge roomKnowledge, DatabaseBridge databaseBridge) {

        mdl = messageDeclaration;
        u = user;
        rk = roomKnowledge;
        dbb = databaseBridge;
    }

    //FIXME if multiple message declarations are to be received, a part of the last buffer
    // (from the previous message decl after the first) will contain part of the message of the message after that.
    // make sure for the last chunk we only get the relevant part for our message declaration (otherwise, cascading failure will occur)
    public void handle() {
        Synchronization sync = u.getSynchronization();
        try {
            sync.receiveLock();
            DataInputStream dis = u.getNetworking().getCurrentUserSocket().getDataInputStream();

            if (!mdl.getContentType().isFile()) readText(dis);
            if (mdl.getContentType().isFile())  readFile(dis);

            rk.increaseContentSizeReceived(mdl.getBodySize());
            rk.incrementMessageReceived();

            Log.d(TAG + ".handle", "Receive successful! bodysize: " + mdl.getBodySize());
        } catch (OversizedMessage om) {
            Log.e(TAG + ".handle", "Oversized message w/ bodysize " + mdl.getBodySize() + " from " + u.getIDENTIFIER(), om);
        } catch (SocketTimeoutException e) {
            Log.e(TAG + ".handle", "Timeout exception when reading " + (mdl.getContentType().isFile()?"file":"text"), e);
        } catch (IOException e) {
            Log.e(TAG + ".handle", "IOException w/ bodysize " + mdl.getBodySize() + " from " + u.getIDENTIFIER(), e);
            try {
                u.getNetworking().shutdown();
            } catch (IOException ioException) {
                Log.e(TAG + ".handle", "Shutdown failed when IOException is thrown?", ioException);
            }
        } catch (InterruptedException e) {
            Log.e(TAG + ".handle", "InboundHandler interrupted uid " + u.getIDENTIFIER(), e);
        } finally {
            sync.receiveUnlock();
        }

    }

    private void readText(DataInputStream dis) throws OversizedTextMessage, IOException {
        if (mdl.getBodySize() > IOManager.MAXIMUM_TEXT_SIZE) throw new OversizedTextMessage();
        int contentSize = Math.toIntExact(mdl.getBodySize());

        int count;
        int totalbytesread = 0;
        byte[] buffer = new byte[Math.toIntExact(mdl.getBodySize())];

        TextProvider provider = new TextProvider(contentSize);
        while(totalbytesread < mdl.getBodySize() && (count = dis.read(buffer)) > 0) {
            totalbytesread += count;
            provider.insertData(buffer, count);
        }

        dbb.onTextReceived(provider, u);
        Log.d(TAG + ".readText", "added dbb text" + provider.getData());
    }

    private void readFile(DataInputStream dis) throws OversizedMultimediaMessage, IOException {
        if (mdl.getBodySize() > IOManager.MAXIMUM_MULTIMEDIA_SIZE) throw new OversizedMultimediaMessage();
        MultimediaProvider provider = new MultimediaProvider(mdl.getHeaderSize(), mdl.getBodySize());

        //HEADER STUFF
        {
            int count;
            int totalbytesread = 0;
            byte[] buffer = new byte[Math.toIntExact(mdl.getHeaderSize())];

            while (totalbytesread < mdl.getHeaderSize() && (count = dis.read(buffer)) > 0) {
                totalbytesread += count;
                provider.insertHeader(buffer, count);
            }
        }

        provider.pre(provider.getHeader());

        //BODY STUFF
        {
            int count;
            int totalbytesread = 0;
            byte[] buffer = new byte[Math.toIntExact(mdl.getHeaderSize())];

            while (totalbytesread < mdl.getBodySize() && (count = dis.read(buffer)) > 0 ) {
                totalbytesread += count;
                provider.insertBody(buffer, count);
            }
        }

        provider.close();

        dbb.onMultimediaReceived(provider, u);
        Log.d(TAG + ".readFile", "added dbb path " + provider.getData());
    }

}

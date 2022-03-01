package networker.messages.io.handlers;

import static networker.messages.content.ContentProvider.Type;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

import helpers.db.DatabaseBridge;
import networker.RoomKnowledge;
import networker.exceptions.OversizedMessage;
import networker.exceptions.OversizedMultimediaMessage;
import networker.exceptions.OversizedTextMessage;
import networker.helpers.NetworkUtilities;
import networker.messages.MessageDeclaration;
import networker.messages.content.ContentProvider;
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
        TextProvider provider = new TextProvider(Math.toIntExact(mdl.getBodySize()));

        procureDataFromStream(dis, provider, Type.BODY, mdl.getBodySize(), IOManager.TEXT_BLOCK_SIZE);

        dbb.onTextReceived(provider, u);
        Log.d(TAG + ".readText", "added dbb text");
    }

    private void readFile(DataInputStream dis) throws OversizedMultimediaMessage, IOException {
        if (mdl.getBodySize() > IOManager.MAXIMUM_MULTIMEDIA_SIZE)
            throw new OversizedMultimediaMessage();
        MultimediaProvider provider = new MultimediaProvider(mdl.getHeaderSize(), mdl.getBodySize());

        procureDataFromStream(dis, provider, Type.HEADER, mdl.getHeaderSize(), IOManager.TEXT_BLOCK_SIZE); //get header
        provider.pre(provider.getHeader()); //initialize file stuff
        procureDataFromStream(dis, provider, Type.BODY, mdl.getBodySize(), IOManager.MULTIMEDIA_BLOCK_SIZE); //get body
        provider.close(); //finalize file stuff

        dbb.onMultimediaReceived(provider, u);
        Log.d(TAG + ".readFile", "added dbb path " + provider.getData());
    }

    private void procureDataFromStream(DataInputStream dis, ContentProvider<?, ?> provider, Type dt, long size, int max) throws IOException {
        int count;
        int totalbytesread = 0;
        byte[] buffer = NetworkUtilities.createBuffer(null, totalbytesread, size, max);

        while (totalbytesread < size && (count = dis.read(buffer)) > 0) {
            totalbytesread += count;
            provider.insertData(buffer, count, dt);
            buffer = NetworkUtilities.createBuffer(buffer, totalbytesread, size, max);
        }
    }

}

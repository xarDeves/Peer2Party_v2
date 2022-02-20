package networker.messages.io.handlers;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;

import helpers.DatabaseBridge;
import networker.RoomKnowledge;
import networker.exceptions.OversizedMultimediaMessage;
import networker.exceptions.OversizedTextMessage;
import networker.messages.MessageDeclaration;
import networker.messages.content.providers.MultimediaProvider;
import networker.messages.content.providers.TextProvider;
import networker.messages.io.IOManager;
import networker.peers.User;

public class InboundHandler {
    private final MessageDeclaration mdl;
    private final User user;
    private final RoomKnowledge rk;
    private final DatabaseBridge dbb;

    //TODO add mediastore/disk API facade
    public InboundHandler(MessageDeclaration messageDeclaration, User user,
                          RoomKnowledge roomKnowledge, DatabaseBridge databaseBridge) {

        mdl = messageDeclaration;
        this.user = user;
        rk = roomKnowledge;
        dbb = databaseBridge;
    }

    public void handle() {
        try {
            user.receiveLock();
            DataInputStream dis = user.getCurrentUserSocket().getDataInputStream();

            if (!mdl.getContentType().isFile()) readText(dis);
            if (mdl.getContentType().isFile()) readFile(dis);

            user.receiveUnlock();

            rk.increaseContentSizeReceived(mdl.getBodySize());
            rk.incrementMessageReceived();
            Log.d("networker.messages.io.handlers.handle", "Receival successful! bodyize: " + mdl.getBodySize());
        } catch (OversizedTextMessage otm) {
            Log.d("networker.messages.io.handlers.handle", "Oversized text message w/ bodysize " + mdl.getBodySize() + " from " + user.getIDENTIFIER(), otm);
        } catch (OversizedMultimediaMessage omm) {
            Log.d("networker.messages.io.handlers.handle", "Oversized multimedia message w/ bodysize " + mdl.getBodySize() + " from " + user.getIDENTIFIER(), omm);
        } catch (IOException e) {
            Log.d("networker.messages.io.handlers.handle" , "IOException w/ bodysize " + mdl.getBodySize() + " from " + user.getIDENTIFIER(), e);
        } catch (InterruptedException e) {
            Log.d("networker.messages.io.handlers.handle", "InboundHandler interrupted uid " + user.getIDENTIFIER(), e);
        }


    }

    private void readText(DataInputStream dis) throws OversizedTextMessage, IOException {
        if (mdl.getBodySize() > IOManager.MAXIMUM_TEXT_SIZE) throw new OversizedTextMessage();
        int contentSize = Math.toIntExact(mdl.getBodySize());

        int count;
        int totalbytesread = 0;
        byte[] buffer = new byte[Math.toIntExact(mdl.getBodySize())];
        TextProvider provider = new TextProvider(contentSize);

        while((count = dis.read(buffer)) > 0 && totalbytesread < mdl.getBodySize()) {
            totalbytesread += count;
            provider.insertData(buffer, count);
        }

        dbb.onTextReceived(provider, user);
    }

    private void readFile(DataInputStream dis) throws OversizedMultimediaMessage, IOException {
        if (mdl.getBodySize() > IOManager.MAXIMUM_MULTIMEDIA_SIZE) throw new OversizedMultimediaMessage();
        MultimediaProvider provider = new MultimediaProvider(mdl.getHeaderSize(), mdl.getBodySize());

        //HEADER STUFF
        {
            int count;
            int totalbytesread = 0;
            byte[] buffer = new byte[Math.toIntExact(mdl.getHeaderSize())];

            while ((count = dis.read(buffer)) > 0 && totalbytesread < mdl.getHeaderSize()) {
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

            while ((count = dis.read(buffer)) > 0 && totalbytesread < mdl.getBodySize()) {
                totalbytesread += count;
                provider.insertBody(buffer, count);
            }
        }

        provider.post();

        dbb.onMultimediaReceived(provider, user);
    }

}

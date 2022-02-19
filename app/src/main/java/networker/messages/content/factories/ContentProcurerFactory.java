package networker.messages.content.factories;

import static networker.io.IOManager.MULTIMEDIA_BLOCK_SIZE;
import static networker.io.IOManager.TEXT_BLOCK_SIZE;

import java.io.FileNotFoundException;

import networker.messages.MessageDeclaration;
import networker.messages.content.ContentProcurer;
import networker.messages.content.procurers.MultimediaProcurer;
import networker.messages.content.procurers.TextProcurer;

public class ContentProcurerFactory {
    public static ContentProcurer createProcurer(MessageDeclaration mdl) throws FileNotFoundException {
        if (mdl.getContentType().isFile())
            return new MultimediaProcurer(mdl.getF().getName(), mdl.getF(), MULTIMEDIA_BLOCK_SIZE);

        return new TextProcurer(mdl.getBody(), TEXT_BLOCK_SIZE);
    }
}

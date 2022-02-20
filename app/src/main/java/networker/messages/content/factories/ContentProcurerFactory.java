package networker.messages.content.factories;

import static networker.messages.io.IOManager.MULTIMEDIA_BLOCK_SIZE;
import static networker.messages.io.IOManager.TEXT_BLOCK_SIZE;

import java.io.FileNotFoundException;

import networker.messages.MessageDeclaration;
import networker.messages.content.ContentProcurer;
import networker.messages.content.procurers.MultimediaProcurer;
import networker.messages.content.procurers.TextProcurer;

public class ContentProcurerFactory {
    public static ContentProcurer createProcurer(MessageDeclaration mdl) throws FileNotFoundException {
        if (mdl.getContentType().isFile())
            return new MultimediaProcurer(mdl.getFile().getName(), mdl.getFile(), MULTIMEDIA_BLOCK_SIZE);

        return new TextProcurer(mdl.getBody(), TEXT_BLOCK_SIZE);
    }
}

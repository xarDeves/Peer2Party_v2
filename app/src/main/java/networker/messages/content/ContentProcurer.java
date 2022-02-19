package networker.messages.content;

import java.io.Closeable;
import java.io.IOException;

public interface ContentProcurer extends Closeable {
    byte[] getHeaderPiece();
    byte[] getContentPiece();

    int consume() throws IOException;

    boolean hasNextHeader();
    boolean hasNextContent();

    long getTotalSize();
}

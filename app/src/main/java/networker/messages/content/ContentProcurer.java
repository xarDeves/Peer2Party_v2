package networker.messages.content;

import java.io.IOException;

public interface ContentProcurer {
    byte[] getHeaderPiece();
    byte[] getContentPiece();

    int consume() throws IOException;

    boolean hasNextHeader();
    boolean hasNextContent();

    long getTotalSize();
}

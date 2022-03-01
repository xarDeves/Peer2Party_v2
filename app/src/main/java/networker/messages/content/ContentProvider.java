package networker.messages.content;

import java.io.IOException;

public interface ContentProvider<H, D> {
    void insertData(byte[] buffer, int count, Type dt) throws IOException;

    H getHeader();

    D getData();

    long getTotalSize();

    enum Type {
        HEADER, BODY
    }
}

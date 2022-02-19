package networker.messages.content.procurers;

import androidx.annotation.NonNull;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import networker.helpers.NetworkUtilities;
import networker.messages.content.ContentProcurer;

public class MultimediaProcurer implements ContentProcurer {

    private final byte[] header;
    private final long totalSize;

    private final DataInputStream dis;
    private final byte[] buffer;
    private boolean hasNext = true;
    private boolean hasNextHeader = true;

    public MultimediaProcurer(@NonNull String h, @NonNull File f, int blockSize) throws FileNotFoundException {
        header = NetworkUtilities.convertUTF8StringToBytes(h);

        buffer = new byte[blockSize];
        dis = new DataInputStream(new FileInputStream(f));

        totalSize = header.length + f.length();
    }

    @Override
    public byte[] getHeaderPiece() {
        hasNextHeader = false;
        return header;
    }

    @Override
    public byte[] getContentPiece() {
        return buffer;
    }

    @Override
    public int consume() throws IOException {
        int bRead = dis.read(buffer);
        hasNext = bRead > 0; // when bRead is == 0 from dis.read, we're done reading the file

        return bRead;
    }

    @Override
    public boolean hasNextHeader() {
        return hasNextHeader;
    }

    @Override
    public boolean hasNextContent() {
        return hasNext;
    }

    @Override
    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public void close() throws IOException {
        dis.close();
    }
}

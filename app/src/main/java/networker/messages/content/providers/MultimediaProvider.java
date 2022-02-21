package networker.messages.content.providers;

import java.io.Closeable;
import java.io.IOException;

import networker.helpers.NetworkUtilities;
import networker.messages.content.ContentProvider;

public class MultimediaProvider implements ContentProvider<String, String>, Closeable {

    private final byte[] bNameArray;
    private int startingNameIndex = 0;

    public MultimediaProvider(int headerSize, long bodySize) {
        bNameArray = new byte[headerSize];
    }

    public void pre(String fname) throws IOException {
        //TODO create file in disk
    }

    public void insertHeader(byte[] buffer, int count) {
        System.arraycopy(buffer, 0, bNameArray, startingNameIndex, count);
        startingNameIndex += count;
    }

    public void insertBody(byte[] buffer, int count) {
        //TODO write to disk
    }

    //TODO return bare filename
    @Override
    public String getHeader() {
        return NetworkUtilities.convertBytesToUTF8String(bNameArray);
    }

    //TODO return URI
    @Override
    public String getData() {
        return null;
    }

    @Override
    public long getTotalSize() {
        return 0;
    }

    @Override
    public void close() throws IOException {
        //TODO close file etc.
    }
}

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

    private void insertHeader(byte[] buffer, int count) {
        System.arraycopy(buffer, 0, bNameArray, startingNameIndex, count);
        startingNameIndex += count;
    }

    private void insertBody(byte[] buffer, int count) throws IOException {
        //TODO write to disk
    }

    @Override
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void insertData(byte[] buffer, int count, Type dt) throws IOException {
        switch (dt) {
            case HEADER:
                insertHeader(buffer, count);
                return;
            case BODY:
                insertBody(buffer, count);
                return;
        }
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

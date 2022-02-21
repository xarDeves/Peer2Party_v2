package networker.messages.content.procurers;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;

import networker.helpers.NetworkUtilities;
import networker.messages.content.ContentProcurer;

public class TextProcurer implements ContentProcurer {

    private final long totalSize;
    private final byte[] content;
    private int blockCount = 0;
    private final int contentBlockSize;

    private byte[] buffer;
    private boolean hasNext = true;

    public TextProcurer(@NonNull String body, int blockSize) {
        content = NetworkUtilities.convertUTF8StringToBytes(body);
        contentBlockSize = blockSize;
        totalSize = content.length;
    }

    @Override
    public byte[] getHeaderPiece() {
        return new byte[0];
    }

    @Override
    public byte[] getContentPiece() {
        return buffer;
    }

    @Override
    public int consume() {
        ++blockCount;

        int start = (blockCount-1)*contentBlockSize;
        int end = contentBlockSize*blockCount;

        if (start > content.length) {
            hasNext = false; //we're done
            Log.d("networker.messages.content.procurers.TextProcurer", "consume start: " + start);
            return 0;
        }

        if (end > content.length ) {
            end = content.length; //trim
            hasNext = false; //we're done
            Log.d("networker.messages.content.procurers.TextProcurer", "consume end: " + end);
        }

        buffer = Arrays.copyOfRange(content, start, end);

        return end-start;
    }

    @Override
    public boolean hasNextHeader() {
        return false;
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

    }
}

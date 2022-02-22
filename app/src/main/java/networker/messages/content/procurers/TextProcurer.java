package networker.messages.content.procurers;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;

import networker.helpers.NetworkUtilities;
import networker.messages.content.ContentProcurer;

public class TextProcurer implements ContentProcurer {
    private static final String TAG = "networker.messages.content.procurers:TextProcurer";

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

        hasNext = start > content.length && end > content.length;

        if (start > content.length) {
            Log.e(TAG + ".consume", "consume start: " + start + " content.length " + content.length);
            return 0;
        }

        if (end > content.length) {
            end = content.length; //trim
            Log.d(TAG + ".consume", "consume end: " + end + " content.length " + content.length);
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

}

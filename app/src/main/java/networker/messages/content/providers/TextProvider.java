package networker.messages.content.providers;

import networker.helpers.NetworkUtilities;
import networker.messages.content.ContentProvider;

/** buffer size must remain constant and NOT change!
 *  bodyCount is the full byte size of the contents
 */
public class TextProvider implements ContentProvider<Object, String> {

    private final byte[] bArray;
    private int startingIndex = 0;

    public TextProvider(int bodyCount) {
        bArray = new byte[bodyCount];
    }

    @Override
    public void insertData(byte[] buffer, int count, Type dt) {
        // suppose startingIndex is 0, count is 2, it will
        //  write to 0 and 1 index, and next time start from index 2
        System.arraycopy(buffer, 0, bArray, startingIndex, count);
        startingIndex += count;
    }

    @Override
    public Object getHeader() {
        return null;
    }

    @Override
    public String getData() {
        return NetworkUtilities.convertBytesToUTF8String(bArray);
    }

    @Override
    public long getTotalSize() {
        return NetworkUtilities.convertBytesToUTF8String(bArray).length();
    }
}

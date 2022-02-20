package networker.messages.content.providers;

import networker.messages.content.ContentProvider;

public class MultimediaProvider implements ContentProvider<String, Object> {
    //TODO

    public void insertHeader(String h) {

    }

    public void insertFile(byte[] buffer) {

    }

    @Override
    public String getHeader() {
        return null;
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public int getTotalSize() {
        return 0;
    }
}

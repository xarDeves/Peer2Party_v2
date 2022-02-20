package networker.messages.content.providers;

import networker.messages.content.ContentProvider;

public class MultimediaProvider implements ContentProvider<Object, String> {
    //TODO

    public void insertHeader(Object o) {

    }

    public void insertFile(byte[] buffer) {

    }

    @Override
    public String getHeader() {
        return null;
    }

    //TODO return URI
    @Override
    public String getData() {
        return null;
    }

    @Override
    public int getTotalSize() {
        return 0;
    }
}

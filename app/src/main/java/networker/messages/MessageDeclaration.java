package networker.messages;

import java.io.File;

public class MessageDeclaration {
    private final int headerSize;
    private final int contentSize;
    private final MessageType contentType;
    
    private final String header;
    private final String body;
    private final File f;
    
    public MessageDeclaration(String header, String body, File f,
                              int contentSize, MessageType contentType, int headerSize) {
        this.contentSize = contentSize;
        this.contentType = contentType;
        this.headerSize = headerSize;

        this.header = header;
        this.body = body;
        this.f = f;
    }

    public MessageDeclaration(int contentSize, MessageType contentType, int headerSize) {
        this.contentSize = contentSize;
        this.contentType = contentType;
        this.headerSize = headerSize;

        this.header = null;
        this.body = null;
        this.f = null;
    }

    public MessageDeclaration(int contentSize, MessageType contentType) {
        this.contentSize = contentSize;
        this.contentType = contentType;

        this.headerSize = 0;
        this.header = null;
        this.body = null;
        this.f = null;
    }

    public int getHeaderSize() {
        if (contentType.isFile()) return headerSize;

        throw new NullPointerException("No filenameSize exists");
    }

    public int getContentSize() {
        return contentSize;
    }

    public MessageType getContentType() {
        return contentType;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public File getF() {
        return f;
    }
}

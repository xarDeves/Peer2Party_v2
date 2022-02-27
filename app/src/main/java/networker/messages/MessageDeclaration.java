package networker.messages;

import java.io.File;

public class MessageDeclaration {
    //TODO split into FileMessageDeclaration & TextMessageDeclaration (and make this abstract)
    private final int headerSize;
    private final int bodySize;
    private final MessageType contentType;
    
    private final String header;
    private final String body;
    private final File f;
    
    public MessageDeclaration(String header, String body, File f,
                              int headerSize, int bodySize, MessageType contentType) {
        this.bodySize = bodySize;
        this.contentType = contentType;
        this.headerSize = headerSize;

        this.header = header;
        this.body = body;
        this.f = f;
    }

    public MessageDeclaration(int headerSize, int bodySize, MessageType contentType) {
        this.bodySize = bodySize;
        this.contentType = contentType;
        this.headerSize = headerSize;

        this.header = null;
        this.body = null;
        this.f = null;
    }

    public MessageDeclaration(int bodySize, MessageType contentType) {
        this.bodySize = bodySize;
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

    public long getBodySize() {
        return bodySize;
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

    public File getFile() {
        return f;
    }
}

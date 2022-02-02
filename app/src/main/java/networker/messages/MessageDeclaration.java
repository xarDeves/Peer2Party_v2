package networker.messages;

public class MessageDeclaration {
    private final int contentSize;
    private final MessageType contentType;

    public MessageDeclaration(int contentSize, MessageType contentType) {
        this.contentSize = contentSize;
        this.contentType = contentType;
    }

    public int getContentSize() {
        return contentSize;
    }

    public MessageType getContentType() {
        return contentType;
    }
}

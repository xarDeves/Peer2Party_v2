package networker;

import java.util.LinkedList;

public class MessageDeclaration {
    private final int contentSize;
    private final MessageType contentType;

    private final LinkedList<String> receivers;

    public MessageDeclaration(int contentSize, MessageType contentType, LinkedList<String> receivers) {
        this.contentSize = contentSize;
        this.contentType = contentType;
        this.receivers = receivers;
    }

    public int getContentSize() {
        return contentSize;
    }

    public MessageType getContentType() {
        return contentType;
    }

    public LinkedList<String> getReceivers() {
        return receivers;
    }
}

package networker.messages;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import networker.peers.User;

public class MessageIntent {
    private final User source;

    private int count = 0;
    private final List<MessageDeclaration> messageDeclarations = new LinkedList<>();
    private final LinkedList<String> receivers; // list of IDs

    public MessageIntent(User source, LinkedList<String> receivers) {
        this.source = source;
        this.receivers = receivers;
    }

    public void addMessageDeclaration(MessageDeclaration mdl) {
        messageDeclarations.add(mdl);
        ++count;
    }

    public User getSource() {
        return source;
    }

    public int getCount() {
        return count;
    }

    public Iterator<MessageDeclaration> getMessageDeclarations() {
        return messageDeclarations.listIterator();
    }

    public LinkedList<String> getReceivers() {
        return receivers;
    }
}

package networker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import networker.peers.Peer;
import networker.peers.User;

public class RoomKnowledge {
    // String (ID)
    private final ConcurrentHashMap<String, Peer> map = new ConcurrentHashMap<>(64);
    private final AtomicInteger totalMessagesSent = new AtomicInteger();
    private final AtomicLong totalContentSizeSent = new AtomicLong();

    private final AtomicInteger totalMessagesReceived = new AtomicInteger();
    private final AtomicLong totalContentSizeReceived = new AtomicLong();

    public void addPeer(Peer p) {
        map.put(p.getUser().getIDENTIFIER(), p);
    }

    public boolean hasPeer(Peer p) {
        return map.containsKey(p.getUser().getIDENTIFIER());
    }

    public boolean hasPeer(User u) {
        return map.containsKey(u.getIDENTIFIER());
    }

    public void removePeer(Peer p) {
        map.remove(p.getUser().getIDENTIFIER());
    }

    public Peer getPeer(String k) {
        return map.get(k);
    }

    void incrementMessageSent() {
        totalMessagesSent.incrementAndGet();
    }
    void increaseContentSizeSent(int byteCount) {
        totalContentSizeSent.getAndAdd(byteCount);
    }
    int getMessageSentCount() {
        return totalMessagesSent.get();
    }
    long getContentSentSize() {
        return totalContentSizeSent.get();
    }

    void incrementMessageReceived() {
        totalMessagesReceived.incrementAndGet();
    }
    void increaseContentSizeReceived(int byteCount) {
        totalContentSizeReceived.getAndAdd(byteCount);
    }
    int getMessageReceivedCount() {
        return totalMessagesReceived.get();
    }
    long getContentReceivedSize() {
        return totalContentSizeReceived.get();
    }

}

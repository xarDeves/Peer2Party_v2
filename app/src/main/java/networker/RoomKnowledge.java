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

    public void removePeer(Peer p) {
        map.remove(p.getUser().getIDENTIFIER());
    }

    public boolean hasPeer(Peer p) {
        return map.containsKey(p.getUser().getIDENTIFIER());
    }

    public boolean hasPeer(User u) {
        return map.containsKey(u.getIDENTIFIER());
    }

    public Peer getPeer(String k) {
        return map.get(k);
    }

    public Peer getPeer(User u) {
        return map.get(u.getIDENTIFIER());
    }

    public void incrementMessagesSent() {
        totalMessagesSent.incrementAndGet();
    }
    public void increaseContentSizeSent(long byteCount) {
        totalContentSizeSent.getAndAdd(byteCount);
    }
    public int getMessagesSentCount() {
        return totalMessagesSent.get();
    }
    public long getContentSentSize() {
        return totalContentSizeSent.get();
    }

    public void incrementMessageReceived() {
        totalMessagesReceived.incrementAndGet();
    }
    public void increaseContentSizeReceived(long byteCount) {
        totalContentSizeReceived.getAndAdd(byteCount);
    }
    public int getMessageReceivedCount() {
        return totalMessagesReceived.get();
    }
    public long getContentReceivedSize() {
        return totalContentSizeReceived.get();
    }

}

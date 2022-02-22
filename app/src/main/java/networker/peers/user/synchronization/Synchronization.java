package networker.peers.user.synchronization;

public interface Synchronization {
    void lock() throws InterruptedException;
    void receiveLock() throws InterruptedException;
    void sendLock() throws InterruptedException;
    void receiveUnlock();
    void sendUnlock();
    void unlock();
}

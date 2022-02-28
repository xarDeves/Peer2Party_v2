package networker.peers.user;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import networker.peers.user.synchronization.Synchronization;

class Synchronizer implements Synchronization {

    private final byte RECEIVE_LOCKS = 1;
    private final byte SEND_LOCKS = 1;
    private final AtomicInteger RECV_LOCK_COUNT = new AtomicInteger(0);
    private final AtomicInteger SEND_LOCK_COUNT = new AtomicInteger(0);
    private final AtomicInteger TOTAL_LOCK_COUNT = new AtomicInteger(0);

    private final Semaphore receiveLock = new Semaphore(RECEIVE_LOCKS);
    private final Semaphore sendLock = new Semaphore(SEND_LOCKS);

    Synchronizer() { }

    @Override
    public void lock() throws InterruptedException {
        receiveLock.acquire(RECEIVE_LOCKS);
        sendLock.acquire(SEND_LOCKS);
        TOTAL_LOCK_COUNT.incrementAndGet();
    }

    @Override
    public void receiveLock() throws InterruptedException {
        receiveLock.acquire(RECEIVE_LOCKS);
        RECV_LOCK_COUNT.incrementAndGet();
    }

    @Override
    public void sendLock() throws InterruptedException {
        sendLock.acquire(SEND_LOCKS);
        SEND_LOCK_COUNT.incrementAndGet();
    }

    @Override
    public void receiveUnlock() {
        if (RECV_LOCK_COUNT.get() >= 1)  {
            receiveLock.release(RECEIVE_LOCKS);
            RECV_LOCK_COUNT.decrementAndGet();
        }
    }

    @Override
    public void sendUnlock() {
        if (SEND_LOCK_COUNT.get() >= 1) {
            sendLock.release(SEND_LOCKS);
            SEND_LOCK_COUNT.decrementAndGet();
        }
    }

    @Override
    public void unlock() {
        if (TOTAL_LOCK_COUNT.get() >= 1) {
            receiveLock.release(RECEIVE_LOCKS);
            sendLock.release(SEND_LOCKS);
            TOTAL_LOCK_COUNT.decrementAndGet();
        }
    }
}

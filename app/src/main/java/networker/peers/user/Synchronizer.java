package networker.peers.user;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import networker.peers.user.synchronization.Synchronization;

class Synchronizer implements Synchronization {

    private final byte TOTAL_LOCKS;
    private final byte RECEIVE_LOCKS;
    private final byte SEND_LOCKS;
    private final AtomicInteger RECV_LOCK_COUNT = new AtomicInteger(0);
    private final AtomicInteger SEND_LOCK_COUNT = new AtomicInteger(0);
    private final AtomicInteger TOTAL_LOCK_COUNT = new AtomicInteger(0);

    private final Semaphore ioLock;

    Synchronizer(final byte totalLocks, final byte receiveLocks, final byte sendLocks) {
        TOTAL_LOCKS = totalLocks;
        RECEIVE_LOCKS = receiveLocks;
        SEND_LOCKS = sendLocks;

        ioLock = new Semaphore(TOTAL_LOCKS);
    }

    @Override
    public void lock() throws InterruptedException {
        ioLock.acquire(TOTAL_LOCKS);
        TOTAL_LOCK_COUNT.incrementAndGet();
    }

    @Override
    public void receiveLock() throws InterruptedException {
        ioLock.acquire(RECEIVE_LOCKS);
        RECV_LOCK_COUNT.incrementAndGet();
    }

    @Override
    public void sendLock() throws InterruptedException {
        ioLock.acquire(SEND_LOCKS);
        SEND_LOCK_COUNT.incrementAndGet();
    }

    @Override
    public void receiveUnlock() {
        if (RECV_LOCK_COUNT.get() == 1)  {
            ioLock.release(RECEIVE_LOCKS);
            RECV_LOCK_COUNT.decrementAndGet();
        }
    }

    @Override
    public void sendUnlock() {
        if (SEND_LOCK_COUNT.get() == 1) {
            ioLock.release(SEND_LOCKS);
            SEND_LOCK_COUNT.decrementAndGet();
        }
    }

    @Override
    public void unlock() {
        if (TOTAL_LOCK_COUNT.get() == 1) {
            ioLock.release(TOTAL_LOCKS);
            TOTAL_LOCK_COUNT.decrementAndGet();
        }
    }
}

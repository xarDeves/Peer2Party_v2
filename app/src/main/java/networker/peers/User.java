package networker.peers;

import android.util.Log;

import org.json.JSONException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkUtilities;
import networker.sockets.SocketAdapter;

public class User {
    //TODO split this class into interfaces, such as User Network Interface or something,
    // because currently the class is ginormous

    /* -------------------------- SYNCHRONIZATION STUFF ------------------------ */
    private static final int TOTAL_LOCKS = 3;
    private static final int RECEIVE_LOCKS = 2;
    private static final int SEND_LOCKS = 1;
    private final AtomicInteger RECV_LOCK_COUNT = new AtomicInteger(0);
    private final AtomicInteger SEND_LOCK_COUNT = new AtomicInteger(0);
    private final AtomicInteger TOTAL_LOCK_COUNT = new AtomicInteger(0);
    private final Semaphore ioLock = new Semaphore(TOTAL_LOCKS);

    // holds essential data for each user in the group (IPV6 only)
    /* -------------------------- NETWORK STUFF -------------------------------- */
    private final String IDENTIFIER;

    private final InetAddress address;
    private final int port;
    private SocketAdapter currentUserSocket = null;

    /* ------------------------- AUTHENTICATION STUFF ------------------------- */
    // this is unused, and will remain so, until decentralized auth is added
    private final String uniqueIndentifier = "";

    /* ------------------------- MISCELLANEOUS STUFF ------------------------- */
    private final String username;
    private volatile Status status;

    private final int priority;

    public User(InetAddress adr, String name, int p, Status st, int priority) throws InvalidPortValueException {
        if (!NetworkUtilities.portIsValid(p)) throw new InvalidPortValueException();

        address = adr;
        username = name;
        port = p;
        status = st;

        this.priority = priority;

        Log.d("networker.peers", "Constructor: " + name);
        IDENTIFIER = address.toString();
    }

    public boolean portIsValid() {
        return NetworkUtilities.portIsValid(port);
    }

    public boolean socketIsClosed() {
        return currentUserSocket.isClosed();
    }

    public void lock() throws InterruptedException {
        ioLock.acquire(TOTAL_LOCKS);
        TOTAL_LOCK_COUNT.incrementAndGet();
    }

    public void receiveLock() throws InterruptedException {
        ioLock.acquire(RECEIVE_LOCKS);
        RECV_LOCK_COUNT.incrementAndGet();
    }

    public void sendLock() throws InterruptedException {
        ioLock.acquire(SEND_LOCKS);
        SEND_LOCK_COUNT.incrementAndGet();
    }

    public void receiveUnlock() {
        if (RECV_LOCK_COUNT.get() == 1)  {
            ioLock.release(RECEIVE_LOCKS);
            RECV_LOCK_COUNT.decrementAndGet();
        }
    }

    public void sendUnlock() {
        if (SEND_LOCK_COUNT.get() == 1) {
            ioLock.release(SEND_LOCKS);
            SEND_LOCK_COUNT.decrementAndGet();
        }
    }

    public void unlock() {
        if (TOTAL_LOCK_COUNT.get() == 1) {
            ioLock.release(TOTAL_LOCKS);
            TOTAL_LOCK_COUNT.decrementAndGet();
        }
    }

    public void createUserSocket() throws IOException, InvalidPortValueException {
        if (!portIsValid()) throw new InvalidPortValueException();
        shutdown();
        //FIXME add timeout to socket creation
        currentUserSocket = new SocketAdapter(address, port);
        Log.d("networker.peers.User.createUserSocket", "connected to " + currentUserSocket.log());

        try {
            sendSalutation();
        } catch (JSONException e) {
            Log.e("networker.peers.User.createUserSocket", "sendSalutation() ", e);
        }
    }

    private void shutdown() throws IOException {
        if (currentUserSocket != null) {
            if (!currentUserSocket.isClosed()) currentUserSocket.close();
        }
    }

    public boolean connectionIsUsable() {
        return currentUserSocket != null && !currentUserSocket.isClosed();
    }

    public void replaceSocket(SocketAdapter newSocket) throws IOException {
        shutdown();
        currentUserSocket = newSocket;
        Log.d("networker.peers.User.replaceSocket", "connected to " + currentUserSocket.log());
    }

    public String getUsername() {
        return username;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status newStatus) {
        this.status = newStatus;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address.getHostAddress();
    }

    public String getIDENTIFIER() {
        return IDENTIFIER;
    }

    public int getPriority() {
        return priority;
    }

    public SocketAdapter getCurrentUserSocket() {
        return currentUserSocket;
    }

    public void sendSalutation() throws IOException, JSONException {
        DataOutputStream dos = currentUserSocket.getDataOutputStream();
        dos.write(NetworkUtilities.convertUTF8StringToBytes(NetworkUtilities.getUserSalutationJSON(this).toString()));
        dos.flush();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;

        // the unique identifier comparison is not needed right now, but is here later on for completeness sake
        return getAddress().equals(user.getAddress());
    }

    public boolean hardEquals(User u) {
        if (this == u) return true;
        if (u == null || getClass() != u.getClass()) return false;

        return getIDENTIFIER().equals(u.getIDENTIFIER()) && getAddress().equals(u.getAddress());
    }

}

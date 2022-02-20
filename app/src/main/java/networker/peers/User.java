package networker.peers;

import android.util.Log;

import org.json.JSONException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Semaphore;

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
    private final Semaphore ioLock = new Semaphore(TOTAL_LOCKS);

    // holds essential data for each user in the group (IPV6 only)
    /* -------------------------- NETWORK STUFF -------------------------------- */
    private String IDENTIFIER;

    private final InetAddress address;
    private final int port;
    private SocketAdapter currentUserSocket = null;

    /* ------------------------- AUTHENTICATION STUFF ------------------------- */
    // this is unused, and will remain so, until decentralized auth is added
    private final String uniqueIndentifier = "";

    /* ------------------------- MISCELLANEOUS STUFF ------------------------- */
    private final String username;
    private Status status = Status.UNKNOWN;

    public User(InetAddress adr, String name, int p) throws InvalidPortValueException {
        if (!NetworkUtilities.portIsValid(p)) throw new InvalidPortValueException();

        address = adr;
        username = name;
        Log.d("fuckingdies", "User: " + name);
        port = p;

        instantiateID();
    }

    public User(InetAddress adr, String name, int p, Status st) throws InvalidPortValueException {
        if (!NetworkUtilities.portIsValid(p)) throw new InvalidPortValueException();

        address = adr;
        username = name;
        Log.d("fuckingdies", "User: " + name);
        port = p;
        status = st;

        instantiateID();
    }

    public boolean isUsable() {
        return NetworkUtilities.portIsValid(port);
    }

    public boolean socketIsClosed() {
        return currentUserSocket.isClosed();
    }

    public void lock() throws InterruptedException {
        ioLock.acquire(TOTAL_LOCKS);
    }

    public void receiveLock() throws InterruptedException {
        ioLock.acquire(RECEIVE_LOCKS);
    }

    public void sendLock() throws InterruptedException {
        ioLock.acquire(SEND_LOCKS);
    }

    public void receiveUnlock() throws InterruptedException {
        ioLock.release(RECEIVE_LOCKS);
    }

    public void sendUnlock() throws InterruptedException {
        ioLock.release(SEND_LOCKS);
    }

    public void unlock() {
        ioLock.release(TOTAL_LOCKS);
    }

    public void createUserSocket() throws IOException, InvalidPortValueException, InterruptedException {
        Log.d("networker", "shutdown123123123123123 " + this.getUsername());
        if (!isUsable()) throw new InvalidPortValueException();
        Log.d("networker", "shutdown123123123123123 " + this.getUsername());
        shutdown();

        Log.d("networker", "shutdown & changing socketadatper " + this.getUsername());
        currentUserSocket = new SocketAdapter(address, port);

        try {
            sendSalutation();
        } catch (JSONException e) {
            Log.d("networker.peers.User", "sendSalutation() ", e);
        }

    }

    private void shutdown() throws IOException, InterruptedException {
        Log.d("networker", "shutting down socketadatper " + this.getUsername());

        lock();
        if (currentUserSocket != null) {
            if (!currentUserSocket.isClosed()) currentUserSocket.close();
        }
        unlock();
        // if everything goes well, we'll be out of here in no time
        //  if something goes bad... well, we might be stuck here a few times
    }

    public boolean connectionIsUsable() {
        return currentUserSocket != null && !currentUserSocket.isClosed();
    }

    public void replaceSocket(SocketAdapter newSocket) throws IOException, InterruptedException {
        shutdown();
        currentUserSocket = newSocket;
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
        return address.getHostName();
    }

    public InetAddress getLogicalAddress() {
        return address;
    }

    private void instantiateID() {
        IDENTIFIER = address.toString();
    }

    public String getIDENTIFIER() {
        return IDENTIFIER;
    }

    public SocketAdapter getCurrentUserSocket() {
        return currentUserSocket;
    }

    public void sendSalutation() throws IOException, JSONException {
        DataOutputStream dos = currentUserSocket.getDataOutputStream();
        dos.write(NetworkUtilities.convertUTF8StringToBytes(NetworkUtilities.getUserSalutationJson(this)));
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

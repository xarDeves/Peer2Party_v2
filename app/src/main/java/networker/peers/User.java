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
    private static final int TOTAL_LOCKS = 2;

    // holds essential data for each user in the group (IPV6 only)
    /* -------------------------- NETWORK STUFF -------------------------------- */
    private String IDENTIFIER;

    private final InetAddress address;
    private final int port;
    private SocketAdapter currentUserSocket = null;

    private final Semaphore ioLock = new Semaphore(TOTAL_LOCKS);

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

    public boolean isOnline(SocketAdapter socket) {
        // use created socket to test if the user's online
        return false;
    }

    public boolean isActive() {
        return ioLock.availablePermits() < TOTAL_LOCKS;
    }

    public void lock() throws InterruptedException {
        ioLock.acquire();
    }

    public void unlock() {
        ioLock.release();
    }

    public void createUserSocket() throws IOException, InvalidPortValueException {
        if (!isUsable()) throw new InvalidPortValueException();
        shutdown();

        currentUserSocket = new SocketAdapter(address, port);

        try {
            sendSalutation();
        } catch (JSONException e) {
            Log.d("networker.peers.User", "sendSalutation() ", e);
        }

    }

    private void shutdown() throws IOException {
        while (true) {
            try {
                ioLock.acquire();
                if (currentUserSocket != null) {
                    if (!currentUserSocket.isClosed()) currentUserSocket.close();
                }
                ioLock.release();
                // if everything goes well, we'll be out of here in no time
                //  if something goes bad... well, we might be stuck here a few times
                break;

            } catch (InterruptedException e) {
                Log.d("networker.peers.User", "interrupted", e);
            }
        }
    }

    public boolean connectionIsUsable() {
        return currentUserSocket != null && !currentUserSocket.isClosed();
    }

    public void replaceSocket(SocketAdapter newSocket) throws IOException {
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

    public void sendSalutation() throws IOException, JSONException {
        DataOutputStream os = currentUserSocket.getDataOutputStream();
        os.write(NetworkUtilities.convertUTF8StringToBytes(NetworkUtilities.getUserSalutationJson(this)));
        os.flush();
        os.close();
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

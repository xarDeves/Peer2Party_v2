package networker.peers;

import android.util.Log;

import org.json.JSONException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Objects;

import networker.exceptions.InvalidPortValueException;
import networker.exceptions.UninitializedPeerException;
import networker.helpers.NetworkUtilities;
import networker.sockets.SocketAdapter;

public class User {
    // holds essential data for each user in the group (IPV6 only)
    /* -------------------------- NETWORK STUFF -------------------------------- */
    private String IDENTIFIER;

    private final InetAddress address;
    private int port; // this is the SSL receive/send port of the user, and CAN change
    private SocketAdapter currentUserSocket = null;

    /* ------------------------- AUTHENTICATION STUFF ------------------------- */
    // this is unused, and will remain so, until decentralized auth is added
    private final String uniqueIndentifier = null;

    /* ------------------------- MISCELLANEOUS STUFF ------------------------- */
    private String username;
    private Status status = Status.UNKNOWN;

    public User(InetAddress adr, String name, int p) throws InvalidPortValueException {
        if (p < 1 || p > 65535) throw new InvalidPortValueException();

        address = adr;
        username = name;
        port = p;

        instantiateID();
    }

    public User(InetAddress adr, String name, int p, Status st) throws InvalidPortValueException {
        if (p < 1 || p > 65535) throw new InvalidPortValueException();

        address = adr;
        username = name;
        port = p;
        status = st;

        instantiateID();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isUsable() {
        return port != -1;
    }

    public boolean isOnline(SocketAdapter socket) {
        // use created socket to test if the user's online
        return false;
    }

    public SocketAdapter createUserSocket() throws UninitializedPeerException, IOException {
        if (!isUsable()) throw new UninitializedPeerException();
        shutdownUser();

        currentUserSocket = new SocketAdapter(address, port);

        try {
            sendSalutation();
        } catch (JSONException e) {
            Log.d("networker.peers.User", "sendSalutation() ", e);
        }

        return currentUserSocket;
    }

    public void shutdownUser() throws IOException {
        if (currentUserSocket != null) {
            if (!currentUserSocket.isClosed()) currentUserSocket.close();
        }
    }

    public boolean connectionIsUsable() {
        return currentUserSocket != null && !currentUserSocket.isClosed();
    }

    public void replaceSocket(SocketAdapter newSocket) throws IOException {
        shutdownUser();
        currentUserSocket = newSocket;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
        return address.toString();
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

    //this returns true if port changed
    public boolean updateSelf(User u) {
        if (!NetworkUtilities.portIsValid(u.getPort())) return false; //change nothing, invalid port

        boolean changed = u.getPort() != port;

        port = u.getPort();
        status = u.getStatus();

        return changed;
    }

    public void sendSalutation() throws IOException, JSONException {
        DataOutputStream os = new DataOutputStream(currentUserSocket.getOutputStream());
        os.write(NetworkUtilities.convertUTF8StringToBytes(NetworkUtilities.getUserSalutationJson(this)));
        os.flush();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;

        // the unique identifier comparison is not needed right now, but is here later on for completeness sake
        return getPort() == user.getPort() && getIDENTIFIER().equals(user.getIDENTIFIER()) && getAddress().equals(user.getAddress()) && Objects.equals(uniqueIndentifier, user.uniqueIndentifier) && getUsername().equals(user.getUsername()) && getStatus() == user.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIDENTIFIER(), getAddress(), getPort(), uniqueIndentifier, getUsername(), getStatus());
    }
}

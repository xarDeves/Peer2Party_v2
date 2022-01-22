package networker;

import java.io.IOException;
import java.net.Inet6Address;

import networker.exceptions.UninitializedPeerException;
import networker.sockets.SocketAdapter;

public class User {
    // holds essential data for each user in the group (IPV6 only)
    /* -------------------------- NETWORK STUFF -------------------------------- */
    private final Inet6Address address;
    private int port = -1; // this is the SSL receive/send port of the user, and CAN change
    private SocketAdapter currentUserSocket = null;

    /* ------------------------- AUTHENTICATION STUFF ------------------------- */
    // this is unused, and will remain so, until decentralized auth is added
    private final String uniqueIndentifier = null;

    /* ------------------------- MISCELLANEOUS STUFF ------------------------- */
    private String username;
    private Status status = Status.UNKNOWN;

    User(Inet6Address adr, String name) {
        address = adr;
        username = name;
    }

    User(Inet6Address adr, String name, int p) {
        address = adr;
        username = name;
        port = p;
    }

    User(Inet6Address adr, String name, int p, Status status) {
        address = adr;
        username = name;
        port = p;
        this.status = status;
    }

    User(Inet6Address adr, int p) {
        address = adr;
        port = p;
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
        if (currentUserSocket != null) shutdownUser();

        currentUserSocket = new SocketAdapter(address, port);

        return currentUserSocket;
    }

    public SocketAdapter refreshSocket() throws IOException, UninitializedPeerException {
        if (currentUserSocket != null) {
            if (!currentUserSocket.isClosed()) currentUserSocket.close();
        }

        return createUserSocket();
    }

    public void shutdownUser() throws IOException {
        if (currentUserSocket != null) {
            if (!currentUserSocket.isClosed()) currentUserSocket.close();
        }
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
        return address.getHostName();
    }

}

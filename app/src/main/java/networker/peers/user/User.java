package networker.peers.user;

import android.util.Log;

import java.net.InetAddress;

import networker.exceptions.InvalidPortValueException;
import networker.helpers.NetworkUtilities;
import networker.peers.Status;
import networker.peers.user.auth.Authentication;
import networker.peers.user.network.Networking;
import networker.peers.user.synchronization.Synchronization;

public class User {
    private static final String TAG = "networker.peers.user:User";
    /* -------------------------- SYNCHRONIZATION STUFF ------------------------ */
    private final Synchronization synchronizer = new Synchronizer((byte)3, (byte)2, (byte)1);

    /* -------------------------- NETWORK STUFF -------------------------------- */
    private final Networking networker;

    /* ------------------------- AUTHENTICATION STUFF ------------------------- */
    private final Authentication auth = new Authenticator();

    /* ------------------------- MISCELLANEOUS STUFF ------------------------- */
    private final String IDENTIFIER;
    private final String username;
    private volatile Status status;

    public User(InetAddress adr, String name, int p, Status st, int priority) throws InvalidPortValueException {
        if (!NetworkUtilities.portIsValid(p)) throw new InvalidPortValueException();

        username = name;
        status = st;

        IDENTIFIER = adr.getHostAddress();
        networker = new Networker(adr, p, priority, this);

        Log.d(TAG + ".User", "Created " + name);
    }

    public String getUsername() {
        return username;
    }

    public String getIDENTIFIER() {
        return IDENTIFIER;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status newStatus) {
        this.status = newStatus;
    }

    public Synchronization getSynchronization() {
        return synchronizer;
    }

    public Authentication getAuthentication() {
        return auth;
    }

    public Networking getNetworking() {
        return networker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Networking userNetworking = ((User) o).getNetworking();
        // the unique identifier comparison is not needed right now, but is here later on for completeness sake
        return userNetworking.getHostAddress().equals(getNetworking().getHostAddress());
    }

}

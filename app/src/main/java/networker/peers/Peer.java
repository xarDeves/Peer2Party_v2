package networker.peers;

public class Peer {
    private final User user;
    private volatile boolean enabled = false;

    public Peer(User user) {
        this.user = user;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public User getUser() {
        return user;
    }
}

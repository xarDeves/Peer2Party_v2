package networker.peers;

public enum Status {
    AVAILABLE, AWAY, BUSY, UNKNOWN;

    public static int toInt(Status st) {
        switch(st){
            case AVAILABLE: return 0;
            case AWAY:      return 1;
            case BUSY:      return 2;
            default:        return -1;
        }
    }

    public static Status toStatus(int st) {
        switch(st){
            case 0:  return AVAILABLE;
            case 1:  return AWAY;
            case 2:  return BUSY;
            default: return UNKNOWN;
        }
    }
}

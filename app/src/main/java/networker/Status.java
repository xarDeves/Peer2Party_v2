package networker;

public enum Status {
    AVAILABLE, AWAY, BUSY, UNKNOWN;

    public static int toInt(Status st) {
        if (st.equals(AVAILABLE)) return 0;
        if (st.equals(AWAY)) return 1;
        if (st.equals(BUSY)) return 2;

        return 3; // UNKNOWN
    }

    public static Status toStatus(int st) {
        if (st == 0) return AVAILABLE;
        if (st == 1) return AWAY;
        if (st == 2) return BUSY;

        // st == 3
        return UNKNOWN;
    }
}

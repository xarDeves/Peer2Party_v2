package networker.messages;

public enum MessageType {
    TEXT, IMAGE, GIF, VIDEO, AUDIO, MULTIMEDIA, INVALID;

    public int toInt() {
        if (this.equals(TEXT))       return 0;
        if (this.equals(IMAGE))      return 1;
        if (this.equals(GIF))        return 2;
        if (this.equals(VIDEO))      return 3;
        if (this.equals(AUDIO))      return 4;
        if (this.equals(MULTIMEDIA)) return 5;

        return -1; // wtf?
    }

    public static MessageType intToMessageType(int mdl) {
        if (mdl == 0) return TEXT;
        if (mdl == 1) return IMAGE;
        if (mdl == 2) return GIF;
        if (mdl == 3) return VIDEO;
        if (mdl == 4) return AUDIO;
        if (mdl == 5) return MULTIMEDIA;

        // mdl == -1
        return INVALID;
    }

    public boolean isFile() {
        return this.equals(IMAGE) || this.equals(GIF) || this.equals(VIDEO) || this.equals(AUDIO) || this.equals(MULTIMEDIA);
    }
}

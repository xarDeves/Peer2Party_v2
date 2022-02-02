package networker.messages;

public enum MessageType {
    TEXT, IMAGE, GIF, VIDEO, AUDIO, MULTIMEDIA, INVALID;

    public static int toInt(MessageType mdt) {
        if (mdt.equals(TEXT))       return 0;
        if (mdt.equals(IMAGE))      return 1;
        if (mdt.equals(GIF))        return 2;
        if (mdt.equals(VIDEO))      return 3;
        if (mdt.equals(AUDIO))      return 4;
        if (mdt.equals(MULTIMEDIA)) return 5;

        return -1; // wtf?
    }

    public static MessageType toDeclarationType(int mdl) {
        if (mdl == 0) return TEXT;
        if (mdl == 1) return IMAGE;
        if (mdl == 2) return GIF;
        if (mdl == 3) return VIDEO;
        if (mdl == 4) return AUDIO;
        if (mdl == 5) return MULTIMEDIA;

        // mdl == -1
        return INVALID;
    }
}

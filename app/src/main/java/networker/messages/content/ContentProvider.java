package networker.messages.content;

public interface ContentProvider<H, D> {
    H getHeader();
    D getData();
    int getTotalSize();
}

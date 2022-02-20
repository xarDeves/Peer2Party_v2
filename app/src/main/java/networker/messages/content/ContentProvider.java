package networker.messages.content;

public interface ContentProvider<H, D> {
    H getHeader(); //TODO implement correctly in implementing classes
    D getData(); //TODO implement correctly in implementing classes
    long getTotalSize(); //TODO implement correctly in implementing classes
}

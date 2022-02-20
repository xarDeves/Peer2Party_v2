package networker.messages.io;

import java.io.IOException;

import networker.messages.MessageDeclaration;
import networker.messages.MessageIntent;

public interface MessageManager {
    void discover() throws IOException;
    void receive(MessageIntent mi);
    void send(MessageIntent mi);
}

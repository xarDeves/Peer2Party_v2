package networker.messages.io.processors;

import java.io.IOException;

import networker.messages.MessageIntent;

public interface InboundMessageProcessor {
    void receive(MessageIntent mi);
}

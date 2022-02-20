package networker.messages.io.processors.inbound;

import networker.messages.MessageIntent;

public interface InboundMessageProcessor {
    void receive(MessageIntent mi);
}

package networker.messages.io.processors.outbound;

import networker.messages.MessageIntent;

public interface OutboundMessageProcessor {
    void send(MessageIntent mi);
}

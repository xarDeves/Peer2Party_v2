package networker.messages.io.processors;

import networker.messages.MessageIntent;

public interface OutboundMessageProcessor {
    void send(MessageIntent intent);
}

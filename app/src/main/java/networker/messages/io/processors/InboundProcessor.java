package networker.messages.io.processors;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;

import networker.RoomKnowledge;
import networker.messages.MessageIntent;

public class InboundProcessor implements InboundMessageProcessor {
    public InboundProcessor(@NonNull ExecutorService executorService, RoomKnowledge rk) {

    }

    @Override
    public void receive(MessageIntent mi) {

        //TODO sum data received to rk
    }
}

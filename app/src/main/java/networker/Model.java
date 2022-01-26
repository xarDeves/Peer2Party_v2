package networker;

import android.util.Log;

import java.net.NetworkInterface;
import java.util.LinkedList;

import data.Message;
import data.MessageType;
import helpers.DateTimeHelper;
import networker.helpers.NetworkUtilities;
import viewmodels.MainViewModel;

public class Model {

    private final MainViewModel viewModel;
    private final LinkedList<NetworkInterface> networkInterfaces = new LinkedList<>();

    public Model(MainViewModel viewModel) {
        this.viewModel = viewModel;
        attachSendObserver();

        refreshViableNetworkInterfaces();
    }

    private void attachSendObserver() {
        viewModel.getAllMessages().observeForever(messageList -> {
            if (!messageList.isEmpty()) {
                Message message = messageList.get(messageList.size() - 1);

                switch (message.getMessageType()) {
                    case TEXT_SEND:
                        onTextSend(message);
                        break;
                    case IMAGE_SEND:
                        break;
                }
            }

        });
    }

    public void onTextSend(Message message) {
        Log.d("fuck", message.getPayload());
    }

    //any database (and consequently UI) updates are already multithreaded
    public void onTextReceived() {

        viewModel.insertEntity(new Message(MessageType.TEXT_RECEIVE,
                "text to receive(?)",
                DateTimeHelper.fetchDateTime(),
                "alias")
        );
    }

    public void refreshViableNetworkInterfaces() {
        networkInterfaces.clear();
        NetworkUtilities.getViableNetworkInterfaces(networkInterfaces);
    }

}

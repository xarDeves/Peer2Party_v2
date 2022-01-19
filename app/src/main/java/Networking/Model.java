package Networking;

import android.util.Log;

import data.Message;
import data.MessageType;
import helpers.DateTimeHelper;
import viewmodels.MainViewModel;

public class Model {

    private final MainViewModel viewModel;

    public Model(MainViewModel viewModel) {
        this.viewModel = viewModel;
        attachSendObserver();
    }

    private void attachSendObserver() {
        viewModel.getAllMessages().observeForever(messageList -> {
            Message message = messageList.get(messageList.size() - 1);

            switch (message.getMessageType()) {
                case TEXT_SEND:
                    onTextSend(message);
                    break;
                case IMAGE_SEND:
                    break;
            }

        });
    }

    public void onTextSend(Message message) {
        Log.d("fuck", message.getPayload());
    }

    //any database (and consequently UI) updates are already multithreaded
    public void onTextReceived() {

        viewModel.insertEntity(new Message(MessageType.TEXT_RECEIVE,
                "text to send",
                DateTimeHelper.fetchDateTime(),
                "alias")
        );
    }

}

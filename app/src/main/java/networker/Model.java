package networker;

import android.util.Log;

import data.Message;
import viewmodels.MainViewModel;

public class Model {

    private final MainViewModel viewModel;

    public Model(MainViewModel viewModel) {
        this.viewModel = viewModel;
        attachSendObserver();
    }

    //FIXME not here
    private void attachSendObserver() {
        viewModel.getAllMessages().observeForever(messageList -> {
            if (!messageList.isEmpty()) {
                Message message = messageList.get(messageList.size() - 1);

                switch (message.getMessageType()) {
                    case TEXT_SEND:
                        onTextSend(message);
                        break;
                    case IMAGE_SEND:
                        onImageSend(message);
                        break;
                }
            }

        });
    }

    public void onTextSend(Message message) {
        Log.d("networker.model.onTextSend", message.getPayload());
    }

    public void onImageSend(Message message) {
        Log.d("networker.model.onImageSend", message.getPayload());
    }

}

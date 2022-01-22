package networker;

import android.util.Log;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import data.Message;
import data.MessageType;
import helpers.DateTimeHelper;
import viewmodels.MainViewModel;

public class Model {

    private final MainViewModel viewModel;
    private final List<NetworkInterface> networkInterfaces = new LinkedList<>();

    public Model(MainViewModel viewModel) {
        this.viewModel = viewModel;
        attachSendObserver();

        refreshViableNetworkInterfaces();
    }

    private void attachSendObserver() {
//        viewModel.getAllMessages().observeForever(messageList -> {
//            Message message = messageList.get(messageList.size() - 1);
//
//            switch (message.getMessageType()) {
//                case TEXT_SEND:
//                    onTextSend(message);
//                    break;
//                case IMAGE_SEND:
//                    break;
//            }
//
//        });
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

    //TODO MIGRATE THIS SOMEWHERE ELSE
    public void refreshViableNetworkInterfaces() {
        networkInterfaces.clear();
        // https://stackoverflow.com/a/6238459/10007109
        try {
            for(Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces(); list.hasMoreElements();)  {
                NetworkInterface i = list.nextElement();
                Log.e("networker", "network_interface displayName " + i.getDisplayName());
                networkInterfaces.add(i);
            }
        } catch (SocketException e) {
            Log.d("networker", "NetworkInterface.getNetworkInterfaces()", e);
        }

    }

}

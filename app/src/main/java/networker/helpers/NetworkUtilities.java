package networker.helpers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedList;

import networker.exceptions.InvalidPortValueException;
import networker.messages.MessageDeclaration;
import networker.messages.MessageType;
import networker.peers.Status;
import networker.peers.User;

public class NetworkUtilities {

    public static final int DISCOVERY_BUFFER_SIZE = 2048; // in bytes, 2048 == 2 kilobytes

    public static final String JSON_ADDRESS_IP = "address";
    public static final String JSON_ADDRESS_PORT = "port";
    public static final String JSON_ADDRESS_USERNAME = "username";
    public static final String JSON_ADDRESS_STATUS = "status";

    private static final int MESSAGE_DECLARATION_OVERHEAD_SIZE = 100;
    public static final int MAX_MESSAGE_DECLARATION_BUFFER_SIZE = 8192 + MESSAGE_DECLARATION_OVERHEAD_SIZE;

    public static final String JSON_MCONTENT_SIZE = "msize";
    public static final String JSON_MCONTENT_TYPE = "mtype";
    public static final String JSON_RECIPIENT_ARRAY = "recvs";

    public static byte[] convertUTF8StringToBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static String convertBytesToUTF8String(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static DatagramPacket createDatagramPacket(byte[] buffer, InetAddress group, int port) {
        return new DatagramPacket(buffer, buffer.length, group, port);
    }

    public static String getUserSalutationJson(User u) throws JSONException {
        // https://www.tutorialspoint.com/json/json_java_example.htm

        JSONObject salutation = new JSONObject();
        salutation.put(JSON_ADDRESS_IP, u.getAddress());
        salutation.put(JSON_ADDRESS_PORT, u.getPort());
        salutation.put(JSON_ADDRESS_USERNAME, u.getUsername());
        salutation.put(JSON_ADDRESS_STATUS, Status.toInt(u.getStatus()));

        return salutation.toString();
    }

    public static User processUserSalutationJson(String salutation) throws JSONException, UnknownHostException, InvalidPortValueException {
        JSONObject jObj = new JSONObject(salutation);

        Status status = Status.toStatus(jObj.getInt(JSON_ADDRESS_STATUS));
        String username = jObj.getString(JSON_ADDRESS_USERNAME);
        int port = jObj.getInt(JSON_ADDRESS_PORT);
        InetAddress addr = InetAddress.getByName(jObj.getString(JSON_ADDRESS_IP));

        return new User(addr, username, port, status);
    }

    public static String getMessageDeclarationJson(MessageDeclaration messageDeclaration) throws JSONException {
        JSONObject declaration = new JSONObject();

        declaration.put(JSON_MCONTENT_SIZE, messageDeclaration.getContentSize());
        declaration.put(JSON_MCONTENT_TYPE, MessageType.toInt(messageDeclaration.getContentType()));
        JSONArray array = new JSONArray();

        for (String id : messageDeclaration.getReceivers()) {
            array.put(id);
        }

        declaration.put(JSON_RECIPIENT_ARRAY, array);

        return declaration.toString();
    }

    public static MessageDeclaration processMessageDeclarationJson(String declaration) throws JSONException {
        JSONObject jObj = new JSONObject(declaration);

        int contentSize = jObj.getInt(JSON_MCONTENT_SIZE);
        MessageType messageType = MessageType.toDeclarationType(jObj.getInt(JSON_MCONTENT_TYPE));

        JSONArray jArray = jObj.getJSONArray(JSON_RECIPIENT_ARRAY);

        LinkedList<String> receiverIDs = new LinkedList<>();
        for (int i = 0; i < jArray.length(); i++) {
            receiverIDs.add(jArray.getString(i));
        }

        return new MessageDeclaration(contentSize, messageType, receiverIDs);
    }

    //TODO MIGRATE THIS SOMEWHERE ELSE
    public static void getViableNetworkInterfaces(LinkedList<NetworkInterface> networkInterfaces) {

        // https://stackoverflow.com/a/6238459/10007109
        try {
            for (Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces(); list.hasMoreElements(); ) {
                NetworkInterface i = list.nextElement();
                Log.e("networker", "network_interface displayName " + i.getDisplayName());
                networkInterfaces.add(i);
            }
        } catch (SocketException e) {
            Log.d("networker", "NetworkInterface.getNetworkInterfaces()", e);
        }

    }

    public static boolean portIsValid(int p) {
        return p > 0 && p < 65535;
    }
}

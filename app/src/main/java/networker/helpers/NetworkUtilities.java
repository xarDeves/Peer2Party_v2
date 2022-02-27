package networker.helpers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

import networker.exceptions.InvalidPortValueException;
import networker.messages.MessageDeclaration;
import networker.messages.MessageIntent;
import networker.messages.MessageType;
import networker.peers.Status;
import networker.peers.user.User;
import networker.peers.user.network.Networking;
import networker.sockets.SocketAdapter;

public class NetworkUtilities {
    private static final String TAG = "networker.helpers:NetworkUtilities";

    public static final int DISCOVERY_BUFFER_SIZE = 256; // in bytes, 256 == 2 kilobytes

    public static final String JSON_ADDRESS_IP = "address";
    public static final String JSON_ADDRESS_PORT = "port";
    public static final String JSON_ADDRESS_USERNAME = "username";
    public static final String JSON_ADDRESS_STATUS = "status";
    public static final String JSON_ADDRESS_PRIOTIY = "priority";

    // users should also declare themselves along with the message declaration(s)
    public static final int MAX_MESSAGE_DECLARATION_BUFFER_SIZE = 4096 + DISCOVERY_BUFFER_SIZE;

    public static final String JSON_USER_DECLARATION = "declr";
    public static final String JSON_MESSAGE_COUNT = "count";
    public static final String JSON_MTITLE_SIZE = "mtsize";
    public static final String JSON_MCONTENT_SIZE = "msize";
    public static final String JSON_MCONTENT_TYPE = "mtype";
    public static final String JSON_RECIPIENT_ARRAY = "recvs";

    public static JSONObject createSalutationJSON(User u) throws JSONException {
        // https://www.tutorialspoint.com/json/json_java_example.htm

        JSONObject salutation = new JSONObject();
        Networking net = u.getNetworking();
        salutation.put(JSON_ADDRESS_IP, net.getHostAddress());
        salutation.put(JSON_ADDRESS_PORT, net.getPort());
        salutation.put(JSON_ADDRESS_USERNAME, u.getUsername());
        salutation.put(JSON_ADDRESS_STATUS, Status.toInt(u.getStatus()));
        salutation.put(JSON_ADDRESS_PRIOTIY, net.getPriority());

        return salutation;
    }

    public static User processUserSalutationJSON(String salutation) throws JSONException, UnknownHostException, InvalidPortValueException {
        JSONObject jObj = new JSONObject(salutation);

        return processUserSalutationJSON(jObj);
    }

    public static User processUserSalutationJSON(JSONObject jObj) throws JSONException, UnknownHostException, InvalidPortValueException {
        int priority = jObj.getInt(JSON_ADDRESS_PRIOTIY);
        Status status = Status.toStatus(jObj.getInt(JSON_ADDRESS_STATUS));
        String username = jObj.getString(JSON_ADDRESS_USERNAME);
        int port = jObj.getInt(JSON_ADDRESS_PORT);
        InetAddress addr = InetAddress.getByName(jObj.getString(JSON_ADDRESS_IP));

        return new User(addr, username, port, status, priority);
    }

    public static MessageIntent processMessageIntent(String message) throws JSONException, InvalidPortValueException, UnknownHostException {
        JSONObject jObj = new JSONObject(message);
        /* --------------- GET USER DECLARATION AND MESSAGE COUNT --------------- */
        User src = processUserSalutationJSON(jObj.getJSONObject(JSON_USER_DECLARATION));
        int mdlSize = jObj.getInt(JSON_MESSAGE_COUNT);

        /* --------------- GET RECEIVER IDs --------------- */
        JSONArray jArray = jObj.getJSONArray(JSON_RECIPIENT_ARRAY);
        LinkedList<String> receiverIDs = new LinkedList<>();
        for (int i = 0; i < jArray.length(); i++) {
            receiverIDs.add(jArray.getString(i));
        }

        /* --------------- GET MESSAGE DECLARATIONS --------------- */
        MessageIntent msg = new MessageIntent(src, receiverIDs);
        for (int i = 0; i < mdlSize; ++i) {
            //get mdl
            JSONObject jMdl = jObj.getJSONObject(String.valueOf(i));
            //process each mdl individually, and treat it as a single message declaration
            msg.addMessageDeclaration(processSingularMessageDeclaration(jMdl));
        }

        return msg;
    }

    private static MessageDeclaration processSingularMessageDeclaration(JSONObject jObj) throws JSONException {
        int contentSize = jObj.getInt(JSON_MCONTENT_SIZE);
        MessageType messageType = MessageType.intToMessageType(jObj.getInt(JSON_MCONTENT_TYPE));

        if (messageType.isFile())
            return new MessageDeclaration(jObj.getInt(JSON_MTITLE_SIZE), contentSize, messageType);

        return new MessageDeclaration(contentSize, messageType);
    }

    public static String createMessageIntentJSON(MessageIntent msg) throws JSONException {
        JSONObject declaration = new JSONObject();
        /* --------------- PUT USER DECLARATION AND MESSAGE COUNT --------------- */
        declaration.put(JSON_USER_DECLARATION, createSalutationJSON(msg.getSource()));
        declaration.put(JSON_MESSAGE_COUNT, msg.getCount());

        /* --------------- PUT RECEIVER IDs --------------- */
        JSONArray array = new JSONArray();
        for (String id : msg.getReceivers()) {
            array.put(id);
        }
        declaration.put(JSON_RECIPIENT_ARRAY, array);

        /* --------------- PUT MESSAGE DECLARATIONS --------------- */
        int count = 0;
        Iterator<MessageDeclaration> it = msg.getMessageDeclarations();
        while (it.hasNext()) {
            MessageDeclaration mdl = it.next();
            // json of jsons
            declaration.put(String.valueOf(count), createMessageDeclarationJSON(mdl));
        }

        return declaration.toString();
    }

    private static JSONObject createMessageDeclarationJSON(MessageDeclaration messageDeclaration) throws JSONException {
        JSONObject declaration = new JSONObject();

        declaration.put(JSON_MCONTENT_SIZE, messageDeclaration.getBodySize());
        declaration.put(JSON_MCONTENT_TYPE, messageDeclaration.getContentType().toInt());

        if (messageDeclaration.getContentType().isFile())
            declaration.put(JSON_MTITLE_SIZE, messageDeclaration.getHeaderSize());

        return declaration;
    }

    public static byte[] convertUTF8StringToBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static String convertBytesToUTF8String(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static DatagramPacket createDatagramPacket(byte[] buffer, InetAddress group, int port) {
        return new DatagramPacket(buffer, buffer.length, group, port);
    }

    //port might be different user to socket, since listening port is different than the one contacting us
    public static boolean userIsConsistentToSocket(User u, SocketAdapter s) {
        return Objects.equals(s.getInetAddress().getHostAddress(), u.getNetworking().getHostAddress());
    }

    public static boolean portIsValid(int p) {
        return p > 0 && p < 65535;
    }

    public static void createConnectionIfThereIsNone(User receiver, User sender) throws IOException, InterruptedException, InvalidPortValueException, JSONException {
        Networking net = receiver.getNetworking();
        if (net.connectionIsUsable()) return;

        net.createUserSocket();
        NetworkUtilities.sendSalutation(receiver, sender);
        Log.d(TAG + ".createConnectionIfThereIsNone", "Created new connection " + net.getCurrentUserSocket().log());
    }

    public static void sendSalutation(User receiver, User sender) throws IOException, JSONException {
        DataOutputStream dos = receiver.getNetworking().getCurrentUserSocket().getDataOutputStream();
        dos.write(NetworkUtilities.convertUTF8StringToBytes(NetworkUtilities.createSalutationJSON(sender).toString()));
        dos.flush();
    }

    public static HashMap<String, NetworkInterface> getViableNetworkInterfaces() {
        HashMap<String, NetworkInterface> networkInterfaces = new HashMap<>();

        // https://stackoverflow.com/a/6238459/10007109
        try {
            for (Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces(); list.hasMoreElements(); ) {
                NetworkInterface i = list.nextElement();

                if (i.isLoopback()) continue;
                if (i.getDisplayName().contains("rmnet")) continue;

                Log.e(TAG + ".getViableNetworkInterfaces", "network_interface displayName " + i.getDisplayName());
                Enumeration<InetAddress> il = i.getInetAddresses();

                int j = 0;
                while(il.hasMoreElements()) {
                    InetAddress ia = il.nextElement();
                    Log.e(TAG + ".getViableNetworkInterfaces", "network_interface InetAddress.getHostAddress " + ia.getHostAddress());
                    ++j;
                }

                // no inet addresses specified, or only one of ipv4/ipv6 is enabled. regardless, a normal interface should support both
                if (j <= 1) continue;

                networkInterfaces.put(i.getDisplayName(), i);
            }
        } catch (SocketException e) {
            Log.e(TAG + ".getViableNetworkInterfaces", "", e);
        }

        return networkInterfaces;
    }
}

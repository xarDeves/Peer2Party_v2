package networker.helpers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

import networker.exceptions.InvalidPortValueException;
import networker.messages.MessageDeclaration;
import networker.messages.MessageIntent;
import networker.messages.MessageType;
import networker.peers.Status;
import networker.peers.User;
import networker.sockets.SocketAdapter;

public class NetworkUtilities {

    public static final int DISCOVERY_BUFFER_SIZE = 256; // in bytes, 256 == 2 kilobytes

    public static final String JSON_ADDRESS_IP = "address";
    public static final String JSON_ADDRESS_PORT = "port";
    public static final String JSON_ADDRESS_USERNAME = "username";
    public static final String JSON_ADDRESS_STATUS = "status";

    // users should also declare themselves along with the message declaration(s)
    public static final int MAX_MESSAGE_DECLARATION_BUFFER_SIZE = 2048 + DISCOVERY_BUFFER_SIZE;

    public static final String JSON_USER_DECLARATION = "declr";
    public static final String JSON_MESSAGE_COUNT = "count";
    public static final String JSON_MTITLE_SIZE = "mtsize";
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

    public static String getDeclarationBroadcast(MessageIntent msg) throws JSONException {
        JSONObject declaration = new JSONObject();
        /* --------------- PUT USER DECLARATION AND MESSAGE COUNT --------------- */
        declaration.put(JSON_USER_DECLARATION, getUserSalutationJson(msg.getSource()));
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
            declaration.put(String.valueOf(count), getSingularMessageDeclaration(mdl));
        }

        return declaration.toString();
    }

    private static JSONObject getSingularMessageDeclaration(MessageDeclaration messageDeclaration) throws JSONException {
        JSONObject declaration = new JSONObject();

        declaration.put(JSON_MCONTENT_SIZE, messageDeclaration.getBodySize());
        declaration.put(JSON_MCONTENT_TYPE, messageDeclaration.getContentType().toInt());

        if (messageDeclaration.getContentType().isFile())
            declaration.put(JSON_MTITLE_SIZE, messageDeclaration.getHeaderSize());

        return declaration;
    }

    public static MessageIntent processMessageIntent(String message) throws JSONException, InvalidPortValueException, UnknownHostException {
        JSONObject jObj = new JSONObject(message);
        /* --------------- GET USER DECLARATION AND MESSAGE COUNT --------------- */
        User src = processUserSalutationJson(jObj.getJSONObject(JSON_USER_DECLARATION).toString());
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

    //port might be different user to socket, since listening port is different than the one contacting us
    public static boolean userDataIsConsistentToSocket(SocketAdapter s, User u) {
        return s.getInetAddress() == u.getLogicalAddress();
    }

    public static boolean portIsValid(int p) {
        return p > 0 && p < 65535;
    }

    public static void createConnectionIfThereIsNone(User u) throws IOException, InterruptedException, InvalidPortValueException {
        if (u.isUsable()) return;
        if (u.getCurrentUserSocket() != null && !u.getCurrentUserSocket().isClosed()) return;

        u.createUserSocket();
    }

    public static HashMap<String, NetworkInterface> getViableNetworkInterfaces() {
        HashMap<String, NetworkInterface> networkInterfaces = new HashMap<>();

        // https://stackoverflow.com/a/6238459/10007109
        try {
            for (Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces(); list.hasMoreElements(); ) {
                NetworkInterface i = list.nextElement();
                if (i.isLoopback()) continue;
                if (i.getDisplayName().contains("rmnet")) continue;

                Log.e("networker.helpers.getViableNetworkInterfaces", "network_interface displayName " + i.getDisplayName());
                Log.e("networker.helpers.getViableNetworkInterfaces", "network_interface Name " + i.getName());
                Log.e("networker.helpers.getViableNetworkInterfaces", "network_interface InetAddresses ");
                Enumeration<InetAddress> il = i.getInetAddresses();

                int j = 0;
                while(il.hasMoreElements()) {
                    InetAddress ia = il.nextElement();
                    Log.e("networker.helpers.getViableNetworkInterfaces", "network_interface address toString" + ia.toString());
                    Log.e("networker.helpers.getViableNetworkInterfaces", "network_interface address getHostAddress " + ia.getHostAddress());
                    ++j;
                }

                // no inet addresses specified, or only one of ipv4/ipv6 is enabled. regardless, a normal interface should support both
                if (j <= 1) continue;

                networkInterfaces.put(i.getDisplayName(), i);
            }
        } catch (SocketException e) {
            Log.d("networker.helpers.getViableNetworkInterfaces", "NetworkInterface.getNetworkInterfaces()", e);
        }

        return networkInterfaces;
    }
}

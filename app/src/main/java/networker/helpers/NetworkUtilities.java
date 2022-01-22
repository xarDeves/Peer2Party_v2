package networker.helpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import networker.Status;
import networker.User;

public class NetworkUtilities {

    public static final int DISCOVERY_BUFFER_SIZE = 8192; // in bytes, 8192 == 8 kilobytes

    public static String getUserSalutation(User u) throws JSONException {
        // https://www.tutorialspoint.com/json/json_java_example.htm

        JSONObject salutation = new JSONObject();
        salutation.put("address", u.getAddress());
        salutation.put("port", u.getPort());
        salutation.put("username", u.getUsername());
        salutation.put("status", Status.toInt(u.getStatus()));

        return salutation.toString();
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
}

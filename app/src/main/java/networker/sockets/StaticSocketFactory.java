package networker.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class StaticSocketFactory {

    static ServerSocket createServerSocket(InetAddress addr, int port, int backlog) throws IOException {
        return new ServerSocket(port, backlog, addr);
    }

    static Socket createSocket(InetAddress addr, int port) throws IOException {
        return new Socket(addr, port);
    }

}

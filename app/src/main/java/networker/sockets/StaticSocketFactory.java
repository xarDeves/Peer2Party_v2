package networker.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class StaticSocketFactory {

    static ServerSocket createServerSocket() throws IOException {
        return new ServerSocket();
    }

    static ServerSocket createServerSocket(InetAddress addr, int port, int backlog) throws IOException {
        return new ServerSocket(port, backlog, addr);
    }

    static Socket createSocket() {
        return new Socket();
    }

    static Socket createSocket(InetAddress addr, int port) throws IOException {
        return new Socket(addr, port);
    }

    public MulticastSocket createMulticastSocket(InetSocketAddress address) throws IOException {
        return new MulticastSocket(address);
    }

    public MulticastSocket createMulticastSocket(InetAddress inetAddress, int port) throws IOException {
        return new MulticastSocket(new InetSocketAddress(inetAddress, port));
    }

}

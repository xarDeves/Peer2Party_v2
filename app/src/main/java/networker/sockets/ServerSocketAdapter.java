package networker.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;

public class ServerSocketAdapter {
    private ServerSocket socket = null;

    public ServerSocketAdapter(ServerSocket s) {
        socket = s;
    }

    public ServerSocketAdapter(InetAddress address, int port, int backlog) throws IOException {
        socket = StaticSocketFactory.createServerSocket(address, port, backlog);
    }

    public void setSoTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
    }

    public SocketAdapter accept() throws IOException {
        return new SocketAdapter(socket.accept());
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public void close() throws IOException {
        socket.close();
    }

    //add other stuff as things go on...

}

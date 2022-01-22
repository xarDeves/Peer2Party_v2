package networker.sockets;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.Socket;

public class SocketAdapter {
    private Socket socket = null;

    public SocketAdapter(Socket s) {
        socket = s;
    }

    public SocketAdapter() {

    }

    public SocketAdapter(Inet6Address address, int port) throws IOException {
        socket = StaticSocketFactory.createSocket(address, port);
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() throws IOException {
        socket.close();
    }

    //add other stuff as things go on...

}

package networker.sockets;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.ServerSocket;

public class ServerSocketAdapter {
    private ServerSocket socket = null;

    public ServerSocketAdapter(ServerSocket s) {
        socket = s;
    }

    public ServerSocketAdapter() {

    }

    public ServerSocketAdapter(Inet6Address address, int port, int backlog) throws IOException {
        socket = StaticSocketFactory.createServerSocket(address, port, backlog);
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() throws IOException {
        socket.close();
    }

    //add other stuff as things go on...

}

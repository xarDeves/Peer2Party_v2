package networker.sockets;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;

public class ServerSocketAdapter implements Closeable, Loggable {
    private static final String TAG = "networker.sockets:ServerSocketAdapter";
    private final ServerSocket socket;

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

    @Override
    public String log() {
        return "host addr " + socket.getInetAddress().getHostAddress() +
                " local SocketAddress " + socket.getLocalSocketAddress().toString() +
                " local port " + socket.getLocalPort();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

}

package networker.sockets;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class SocketAdapter implements Closeable, Loggable {
    private final Socket socket;

    public SocketAdapter(Socket s) {
        socket = s;
    }

    public SocketAdapter(InetAddress address, int port) throws IOException {
        socket = StaticSocketFactory.createSocket(address, port);
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public void setTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
    }

    public int getPort() {
        return socket.getPort();
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public DataOutputStream getDataOutputStream() throws IOException {
        return new DataOutputStream(socket.getOutputStream());
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public DataInputStream getDataInputStream() throws IOException {
        return new DataInputStream(socket.getInputStream());
    }

    @Override
    public String log() {
        return "host addr " + socket.getInetAddress().getHostAddress() + " port " +
                socket.getPort() + " local address " + socket.getLocalAddress() + " local port " +
                socket.getLocalPort();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    //add other stuff as things go on...

}

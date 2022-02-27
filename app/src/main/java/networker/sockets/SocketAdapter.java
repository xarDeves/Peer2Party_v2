package networker.sockets;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

public class SocketAdapter implements Closeable, Loggable {
    private final Socket socket;

    public SocketAdapter() {
        socket = new Socket();
    }

    public SocketAdapter(Socket s) {
        socket = s;
    }

    public SocketAdapter(InetAddress address, int port) throws IOException {
        socket = StaticSocketFactory.createSocket(address, port);
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public void connect(SocketAddress addr, int soTimeout) throws IOException {
        socket.connect(addr, soTimeout);
    }

    public void setTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
    }

    public int getPort() {
        return socket.getPort();
    }

    public DataOutputStream getDataOutputStream() throws IOException {
        return new DataOutputStream(socket.getOutputStream());
    }

    public DataInputStream getDataInputStream() throws IOException {
        return new DataInputStream(socket.getInputStream());
    }

    @Override
    public String log() {
        return  "remote SoAddr " + socket.getRemoteSocketAddress().toString() + "\nhost addr " +
                socket.getInetAddress().getHostAddress() + " port " + socket.getPort() +
                "\nlocal address " + socket.getLocalAddress() + " local port " +
                socket.getLocalPort() + "\nisclosed " + socket.isClosed();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

}

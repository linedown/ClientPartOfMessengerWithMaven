import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Main {
    public static final String address = "93.100.166.31";
    public static final int port = 23456;

    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static Socket socket;

    public static ObjectOutputStream getOutputStream() {
        return out;
    }

    public static void setOutputStream(ObjectOutputStream out) {
        Main.out = out;
    }

    public static ObjectInputStream getInputStream() {
        return in;
    }

    public static void setInputStream(ObjectInputStream in) {
        Main.in = in;
    }

    public static Socket getSocket() {
        return socket;
    }

    public static void setSocket(Socket socket) {
        Main.socket = socket;
    }

    public static void main(String[] args) {
        new AuthorizationForm();
    }
}

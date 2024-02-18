import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);

        ServerSocket servSock = new ServerSocket(port);
        System.out.println("Waiting incoming connection...");

        while(true) {
            Socket sock = servSock.accept();
            DataInputStream inp = new DataInputStream(sock.getInputStream());
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());

            String x = null;

            try {
                while ((x = inp.readUTF()) != null) {

                    System.out.println(x);
                    out.writeUTF(x.toUpperCase());

                }
            }
            catch(IOException e) {
                System.err.println("Client closed its connection.");
            }
        }
    }


}

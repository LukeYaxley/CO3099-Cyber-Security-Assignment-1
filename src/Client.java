import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[1]); // port for server
        String hostname = args[0]; // hostname for server
        String UserID = args[2];// UserID for user
        String Recepient = null;
        Socket sock = new Socket(hostname, port);
        DataOutputStream out = new DataOutputStream(sock.getOutputStream());
        DataInputStream inp = new DataInputStream(sock.getInputStream());

        Scanner sc = new Scanner(System.in);
        String Line = null;
        System.out.println(String.format("Client Program (user: %s)",UserID));
        System.out.println("-------------------------------");
        System.out.println("Do you want to send a message? [y/n]");
        if (sc.nextLine().toLowerCase().equals(String.valueOf('y'))) {

            System.out.println("Enter the recipient userid:");
            Recepient = sc.nextLine();
            while ((Line = sc.nextLine()) != null && !Line.equalsIgnoreCase("exit")) {


                out.writeUTF(Line);
                System.out.println(inp.readUTF());

            }
        }
        else {

        }
    }
}

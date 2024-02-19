import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        try {


            int port = Integer.parseInt(args[1]); // port for server
            String hostname = args[0]; // hostname for server
            String UserID = args[2];// UserID for user

            String Recepient = null;
            Socket sock = new Socket(hostname, port);
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            DataInputStream inp = new DataInputStream(sock.getInputStream());
            ObjectInputStream inpObj = new ObjectInputStream((sock.getInputStream()));
            out.writeUTF(UserID);


            Scanner sc = new Scanner(System.in);
            String Line = null;
            System.out.printf("Client Program (user: %s)%n", UserID);
            System.out.println("-------------------------------");
            Integer logSize = inp.readInt();
            System.out.printf("You have %d messages%n", logSize);
            if (logSize != 0) {
                List<Message> userMessages = new ArrayList<Message>();

                for (int i = 0; i <= logSize; i++) {

                    Object obj = inpObj.readObject();
                    if (obj instanceof Message) {
                        userMessages.add((Message) obj);
                    } else {
                        System.out.println("Not a valid Message");
                    }
                    for (Message userMessage : userMessages) {

                        System.out.printf("%n Date:  %s %n", userMessage.getTimestamp());
                        System.out.printf(" Message:  %s %n%n", userMessage.getContent());

                    }
                    i++;

                }

            }

            System.out.println("Do you want to add a post? [y/n]");
            if (sc.nextLine().toLowerCase().equals(String.valueOf('y'))) {

                System.out.println("Enter the recipient userid:");
                Recepient = sc.nextLine();
                out.writeUTF(Recepient);
                System.out.println("Enter your message:");
                out.writeUTF(sc.nextLine());


            }
        }



        catch(
    SocketException e)

    {
        System.out.println("Lost Connection.");
    } catch(
    ClassNotFoundException e)

    {
        throw new RuntimeException(e);
    }
    }
}




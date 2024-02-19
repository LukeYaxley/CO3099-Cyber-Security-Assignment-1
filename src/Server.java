import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.ObjectOutputStream;

public class Server {
    public static List<Message> userMessages = new ArrayList<Message>();
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);

        ServerSocket servSock = new ServerSocket(port);
        System.out.println("Server online...");

        while(true) {
            Socket sock = servSock.accept();

                DataInputStream inp = new DataInputStream(sock.getInputStream());
                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                ObjectOutputStream outObj = new ObjectOutputStream(sock.getOutputStream());
                String senderName = inp.readUTF();
                System.out.println(String.format("login from user %s", senderName));

                //check messages from user
                List<Message> myMessages = CheckMessages(senderName);
                //send out messages
                out.writeInt(myMessages.size());
                System.out.printf("Delivering %d messages %n",myMessages.size());
                for (Message message : myMessages) {
                    outObj.writeObject(message);
                }

                //get new messages
                String Recepient = inp.readUTF();
                System.out.printf("Sending Message to %s %n",Recepient);
                String message = inp.readUTF();
                System.out.printf("Message content: %s %n",message);
                Message log = new Message(senderName, message, Recepient);
                userMessages.add(log);

                String x = null;



        }
    }

    public static List<Message> CheckMessages(String UserID){
        List<Message> myMessages = new ArrayList<Message>();
        for (Message message : userMessages){

            if (message.getRecepient().equals(UserID)){
                myMessages.add(message);
            }
        }
        return myMessages;
    }



}

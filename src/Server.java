import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public class Server {
    static byte[] Encrypt(PublicKey key, String string) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.update(string.getBytes(StandardCharsets.UTF_8));
    }
    static String Decrypt(PrivateKey key, byte[]rawInput) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] stringBytes = cipher.doFinal(rawInput);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }
    public static List<Message> userMessages = new ArrayList<Message>();
    public static void main(String[] args) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        int port = Integer.parseInt(args[0]);

        ServerSocket servSock = new ServerSocket(port);
        System.out.println("Server online...");

        //generate private key
        String filepath = "server.prv";
        File f = new File(filepath);
        byte[] keyBytes = Files.readAllBytes(f.toPath());
        PKCS8EncodedKeySpec prvSpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey prvKey = kf.generatePrivate(prvSpec);


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
                System.out.printf("Message content: %s %n",Decrypt(prvKey,message.getBytes(StandardCharsets.UTF_8)));

                Message log = new Message(senderName, message, Recepient);
                userMessages.add(log);

                String x = null;



        }
    }

    public static List<Message> CheckMessages(String UserID){
        //decrypt UserId



        List<Message> myMessages = new ArrayList<Message>();
        for (Message message : userMessages){

            if (message.getRecepient().equals(UserID)){
                myMessages.add(message);
            }
        }
        return myMessages;
    }



}

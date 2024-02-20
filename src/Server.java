import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

public class Server {

    static String toHex(byte[] input) {
        StringBuilder sb = new StringBuilder();
        for (byte b : input) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    public static List<Message> userMessages = new ArrayList<>();
    public static void main(String[] args) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        int port = Integer.parseInt(args[0]);


        ServerSocket servSock = new ServerSocket(port);
        System.out.println("Server online...");

        while(true) {
            try {
                //connecting to client
                Socket sock = servSock.accept();

                DataInputStream inp = new DataInputStream(sock.getInputStream());
                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                String senderName = inp.readUTF();
                System.out.printf("login from user %s%n", senderName);

                //Generate server private key
                File f = new File("server.prv");
                byte[] keyBytes = Files.readAllBytes(f.toPath());
                PKCS8EncodedKeySpec prvSpec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PrivateKey prvKey = kf.generatePrivate(prvSpec);



                //check messages from user
                List<Message> myMessages = CheckMessages(senderName);
                //send out messages
                out.writeInt(myMessages.size());
                System.out.printf("Delivering %d messages... %n", myMessages.size());



                for (Message message : myMessages) {

                    // create signature
                    Signature sig = Signature.getInstance("SHA256withRSA");
                    sig.initSign(prvKey);
                    Base64.Encoder encoder = Base64.getEncoder();
                    String stringContent = encoder.encodeToString(message.getContent());
                    sig.update((message.getTimestamp()+ stringContent).getBytes());
                    byte[] signature = sig.sign();

                    //send server signature and content
                    out.writeUTF(message.getTimestamp());
                    byte[] content = message.getContent();
                    out.writeInt(content.length);
                    out.write(content);
                    out.writeInt(signature.length);
                    out.write(signature);
                    userMessages.remove(message);
                }

                //get new messages
                String Recepient = inp.readUTF();
                System.out.printf("Sending Message to %s %n", Recepient);
                int length = inp.readInt();
                byte[] message = new byte[length];
                inp.readFully(message);

                //decrypt received message
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, prvKey);
                byte[] stringBytes = cipher.doFinal(message);
                String result = new String(stringBytes, StandardCharsets.UTF_8);

                //re-encrypt for recepient

                // read public key and generate key
                f = new File(Recepient + ".pub");
                keyBytes = Files.readAllBytes(f.toPath());
                X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(keyBytes);
                kf = KeyFactory.getInstance("RSA");
                PublicKey pubKey = kf.generatePublic(pubSpec);
                //encrypt
                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, pubKey);
                byte[] encrypted = cipher.doFinal(result.getBytes(StandardCharsets.UTF_8));

                //hash Recepient
                MessageDigest md = MessageDigest.getInstance("MD5");
                String rec = "gfhk2024:" + Recepient;
                byte[] d2 = md.digest(rec.getBytes());
                Recepient = toHex(d2);


                System.out.printf("Message content: %s %n%n", result);

                Message log = new Message(encrypted, Recepient);
                userMessages.add(log);

            }
            catch (SocketException e){
                System.out.println("Client disconnected");
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
            catch (EOFException e){
                System.out.println("Client stopped communicating");
            }

        }
    }

    public static List<Message> CheckMessages(String UserID){
        List<Message> myMessages = new ArrayList<>();
        for (Message message : userMessages){

            if (message.getRecepient().equals(UserID)){
                myMessages.add(message);
            }
        }
        return myMessages;
    }



}

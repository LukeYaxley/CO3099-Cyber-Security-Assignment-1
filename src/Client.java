import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.*;

import java.util.*;

public class Client {


    static String Decrypt(byte[] input,String User) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        // read key
        File f = new File(User+".prv");
        byte[] keyBytes = Files.readAllBytes(f.toPath());
        PKCS8EncodedKeySpec prvSpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey prvKey = kf.generatePrivate(prvSpec);



        // decrypt
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, prvKey);
        byte[] stringBytes = cipher.doFinal(input);
        return (new String(stringBytes, StandardCharsets.UTF_8));
    }
    static String toHex(byte[] input) {
        StringBuilder sb = new StringBuilder();
        for (byte b : input) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    public static void main(String[] args) throws IOException {
        try {


            int port = Integer.parseInt(args[1]); // port for server
            String hostname = args[0]; // hostname for server
            String UserID = args[2];// UserID for user

            String Recepient;
            Socket sock = new Socket(hostname, port);
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            DataInputStream inp = new DataInputStream(sock.getInputStream());

            //generate public key
            File f = new File("server.pub");
            byte[] keyBytes = Files.readAllBytes(f.toPath());
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pubKey = kf.generatePublic(pubSpec);



            //hash userID
            MessageDigest md = MessageDigest.getInstance("MD5");
            String hash = "gfhk2024:" + UserID;
            byte[] d1 = md.digest(hash.getBytes());
            hash = toHex(d1);
            out.writeUTF(hash);//unencrypted

            Scanner sc = new Scanner(System.in);
            System.out.printf("Client Program (user: %s)%n", UserID);
            System.out.println("-------------------------------");
            Integer logSize = inp.readInt();
            System.out.printf("You have %d messages%n", logSize);
            if (logSize != 0) {
                //get messages

                for (int i = 0; i <= logSize; i++) {



                    //get encrypted contents
                    String timestamp = inp.readUTF();

                    int length = inp.readInt();
                    byte[] content = new byte[length];
                    inp.readFully(content);

                    //get signature
                    int sigSize = inp.readInt();
                    byte[] signature = new byte[sigSize];
                    inp.readFully(signature);

                    // verify signature
                    Signature sig = Signature.getInstance("SHA256withRSA");
                    sig.initVerify(pubKey);
                    Base64.Encoder encoder = Base64.getEncoder();
                    String stringContent = encoder.encodeToString(content);
                    sig.update((timestamp+stringContent).getBytes());

                    if (sig.verify(signature)){
                        //decrypt contents and show user
                        System.out.println("Signature verified");
                        System.out.printf("%n Date:  %s %n", timestamp);//get timestamp
                        System.out.printf(" Message:  %s %n%n", Decrypt(content,UserID));

                        i++;

                    }
                    else{
                        System.out.println("Signature not verified");
                        sock.close();
                        System.out.println("Closing Connection");

                    }




                }

            }

            System.out.println("Do you want to add a post? [y/n]");
            if (sc.nextLine().toLowerCase().equals(String.valueOf('y'))) {

                System.out.println("Enter the recipient userid:");
                Recepient = sc.nextLine();

                out.writeUTF(Recepient);


                System.out.println("Enter your message:");
                String message = sc.nextLine();

                //encrypt message

                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, pubKey);
                byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

                out.writeInt(encrypted.length);
                out.write(encrypted);


            }
        }



        catch(SocketException e) {
        System.out.println("Lost Connection.");
    }  catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | IllegalBlockSizeException |
              BadPaddingException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}




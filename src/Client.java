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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {

    static String toHex(byte[] input) {
        StringBuilder sb = new StringBuilder();
        for (byte b : input) sb.append(String.format("%02X", b));
        return sb.toString();
    }
    static byte[] Encrypt(PublicKey key, String string) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.update(string.getBytes(StandardCharsets.UTF_8));
    }
    static String Decrypt(PrivateKey key, byte[]rawInput) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] stringBytes = cipher.update(rawInput);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

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


            //generate private key
            String filepath = UserID + ".prv";
            File f = new File(filepath);
            byte[] keyBytes = Files.readAllBytes(f.toPath());
            PKCS8EncodedKeySpec prvSpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey prvKey = kf.generatePrivate(prvSpec);


            //hash userID
            MessageDigest md = MessageDigest.getInstance("MD5");
            String hash = "gfhk2024:" + UserID;
            byte[] d1 = md.digest(hash.getBytes());

            hash = toHex(d1);
            out.writeUTF(hash);

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
                        System.out.println("Encrypted Bytes: " + new String(userMessage.getContent().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

                        System.out.printf(" Message:  %s %n%n", Decrypt(prvKey,userMessage.getContent().getBytes(StandardCharsets.UTF_8)));

                    }
                    i++;

                }

            }

            System.out.println("Do you want to add a post? [y/n]");
            if (sc.nextLine().toLowerCase().equals(String.valueOf('y'))) {

                System.out.println("Enter the recipient userid:");
                Recepient = sc.nextLine();

                //generate public key
                filepath = "server.pub";
                System.out.println(filepath);
                f = new File(filepath);
                keyBytes = Files.readAllBytes(f.toPath());
                X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(keyBytes);
                kf = KeyFactory.getInstance("RSA");
                PublicKey pubKey = kf.generatePublic(pubSpec);

                //hash Recepient
                String rec = "gfhk2024:" + Recepient;
                byte[] d2 = md.digest(rec.getBytes());
                Recepient = toHex(d2);

                out.writeUTF(Recepient);


                System.out.println("Enter your message:");
                String message = sc.nextLine();



                byte[] encrypted = Encrypt(pubKey,message);
                out.writeUTF(encrypted.toString());


            }
        }



        catch(SocketException e) {
        System.out.println("Lost Connection.");
    } catch(ClassNotFoundException e) {
        throw new RuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}




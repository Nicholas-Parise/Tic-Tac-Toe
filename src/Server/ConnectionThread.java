package Server;
import java.net.*;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import Game.*;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #4
 * @student Id 7242530
 * @since Dec 11th , 2024
 */

public class ConnectionThread extends Thread{
    private Socket client1,client2;
    private Game g;
    private SecretKey AESkey;

    public ConnectionThread(Socket c1, Socket c2) {
        client1 = c1;
        client2 = c2;

        AESkey = Encryption.generateAES();

        g = new Game();
        g.start();
    }

    public void run() {
        try (
                DataOutputStream out1 = new DataOutputStream(client1.getOutputStream());
                DataOutputStream out2 = new DataOutputStream(client2.getOutputStream());
                DataInputStream in1 = new DataInputStream(client1.getInputStream());
                DataInputStream in2 = new DataInputStream(client2.getInputStream());
        ) {

            encryptionHandshake(in1,out1);
            encryptionHandshake(in2,out2);

            handleInput(in1,1);
            handleInput(in2,2);

            handleOutput(out1, out2);

        } catch (IOException e) {
            System.out.println("Connection TERMINATED");
        }
        System.out.println("ended");
    }

    private void encryptionHandshake(DataInputStream in, DataOutputStream out){

        PublicKey publicKey;

        try {
            // receive the clients public key
            int length = in.readInt();
            byte[] clientKey = new byte[length];
            in.readFully(clientKey);
            System.out.println("receive the clients public key");

            // turn byte client key into public key
            publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(clientKey));
            System.out.println("turn byte client key into public key");

            // encrypt the AES key with the given RSA key
            byte[] encryptedAES = Encryption.encryptWithRSA(AESkey.getEncoded(),publicKey);
            System.out.println("encrypt the AES key with the given RSA key");

            // send the AES key to the client
            out.writeInt(encryptedAES.length);
            out.write(encryptedAES);
            out.flush();
            System.out.println("sent the AES key to the client");
            System.out.println(Base64.getEncoder().encodeToString(AESkey.getEncoded()));

        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void handleInput(DataInputStream in, int player){

        new Thread(()->{
            while (true) {
                try {
                    int length = in.readInt(); // First read the length of the byte array
                    byte[] data = new byte[length];
                    in.readFully(data);

                    //GameData gd = GameData.deSerialize(data);
                    GameData gd = Encryption.decryptWithAES(data,AESkey);

                    System.out.println(System.currentTimeMillis());

                    System.out.println("read in "+player);
                    if(player == 1){
                        g.insertP1Read(gd);
                    }else{
                        g.insertP2Read(gd);
                    }
                } catch (IOException e) {
                    System.out.println("player"+player+" closed connection");
                    synchronized (g) {
                        g.kill();
                    }
                    break; // empty or null object end of file
                }
            }
        }).start();
    }


    private void handleOutput(DataOutputStream out1, DataOutputStream out2){
        try {
            while (true) {
                synchronized (g) {
                    if (g.isWriteReadyP1()) {
                        //byte[] data = GameData.serialize(g.getP1Write());
                        byte[] data = Encryption.encryptWithAES(g.getP1Write(),AESkey);

                        out1.writeInt(data.length); // Send the length of the byte array first
                        out1.write(data);
  //                      out1.writeObject(g.getP1Write());
                       out1.flush();
                    }

                    if (g.isWriteReadyP2()) {
                        //byte[] data = GameData.serialize(g.getP2Write());
                        byte[] data = Encryption.encryptWithAES(g.getP2Write(),AESkey);

                        out2.writeInt(data.length); // Send the length of the byte array first
                        out2.write(data);
                        //out2.writeObject(g.getP2Write());
                        out2.flush();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Connection TERMINATED");
        }
    }


}
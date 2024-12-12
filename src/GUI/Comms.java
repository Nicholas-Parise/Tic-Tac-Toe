package GUI;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #4
 * @student Id 7242530
 * @since Dec 11th , 2024
 */

import Game.Encryption;
import Game.GameData;
import HolePunch.Client;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.*;
import java.io.*;
import java.lang.Thread;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Comms extends Thread{

    boolean canWrite;
    TicTacToe tacToe;
    boolean GameLoop;
    String hostName;
    int portNumber;
    private KeyPair RSAkey;
    SecretKey AESkey;

    public Comms(TicTacToe con){
        tacToe = con;
        GameLoop = true;
        RSAkey = Encryption.generateRSA();
        setIPPort();
    }

    /**
     * uses the HolePunch client to contact the HolePunch server
     * this gets the host name and port number of the private game server
     * If it cannot connect it will end the program.
     */
    public void setIPPort(){
        try {
            String fromServer = Client.getIPPort();
            String[] args = fromServer.split(" ");
            hostName = args[0];
            portNumber = Integer.valueOf(args[1]);
        }catch (IOException e){
            System.out.println("could not establish connection with the Hole Punch Server. \nClosing.....");
        }
    }

    public void kill(){
        GameLoop = false;
    }

    /**
     * Run the communication thread
     * Takes in data from the server and parses it
     * sends data to the server when there is stuff to send in the queue
     */
    public void run() {

        canWrite = false;

        try (
                Socket conn = new Socket(hostName, portNumber);

                DataOutputStream sockOut = new DataOutputStream (conn.getOutputStream());
                DataInputStream sockIn = new DataInputStream(conn.getInputStream());
        ) {

            encryptionHandshake(sockIn, sockOut);

            while (GameLoop) {

                if (Thread.interrupted()) {
                    break;
                }

                if(!canWrite) {

                    try {

                        int length = sockIn.readInt(); // First read the length of the byte array
                        byte[] data = new byte[length];
                        sockIn.readFully(data);

                        //GameData fromServer = GameData.deSerialize(data);
                        GameData fromServer = Encryption.decryptWithAES(data,AESkey);

                        if(fromServer == null){
                            break;
                        }
                        parseInputStream(fromServer);

                    } catch (IOException e) {
                        System.out.println("closed connection");
                        tacToe.GameLoop = false;
                        conn.close();
                        break; // empty or null object end of file
                    }
                }

                try{
                    Thread.sleep(10);
                }catch (Exception e){}

                if(canWrite && tacToe.isDataReady()){
                    canWrite = false;

                    //byte[] data = GameData.serialize(tacToe.getSendToServer());
                    byte[] data = Encryption.encryptWithAES(tacToe.getSendToServer(),AESkey);
                    sockOut.writeInt(data.length); // Send the length of the byte array first
                    sockOut.write(data);
                    sockOut.flush();
                    System.out.println(System.currentTimeMillis());
                }



            }
        } catch (UnknownHostException e) {
            System.out.println("problem with the host name.");
        } catch (IOException e) {
            System.out.println("IO error for the connection.");
        }
        tacToe.kill();
    }


    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }


    /**
     * Reads in data from server and changes necessary variables
     * @param in Game data in
     */
    public synchronized void parseInputStream(GameData in){

        tacToe.setGameState(in.getStatus());
        tacToe.setPlayer(in.getPlayerId());
        tacToe.glh.setMatrix(in.getMatrix());

        switch (in.getStatus()){
            case WIN:
            case LOSE:
                tacToe.setPromptPlayAgain(true);
                break;
            case TURN:
                tacToe.setCanSend(true);
                setCanWrite(true);
                break;
            case PROMPT:
                tacToe.setCanSend(true);
                tacToe.setPromptPlayAgain(true);
                setCanWrite(true);
                tacToe.PlayAgain();
                break;
        }
    }


    private void encryptionHandshake(DataInputStream in, DataOutputStream out){

        try {
            // send the public key to the server unencrypted
            out.writeInt(RSAkey.getPublic().getEncoded().length);
            out.write(RSAkey.getPublic().getEncoded());
            out.flush();
            System.out.println("send the public key to the server unencrypted");

            // receive the servers public key
            int length = in.readInt(); // First read the length of the byte array
            byte[] data = new byte[length];
            in.readFully(data);
            System.out.println("receive the servers public key");

            // decrypt to get our regular byte stream
            byte[] decryptedAES = Encryption.decryptWithRSA(data,RSAkey.getPrivate());
            System.out.println("decrypt to get our regular byte stream");

            // turn byte array into AES key and update
            AESkey = new SecretKeySpec(decryptedAES, 0, decryptedAES.length, "AES");
            System.out.println("turn byte array into AES key and update");
            System.out.println(Base64.getEncoder().encodeToString(AESkey.getEncoded()));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}

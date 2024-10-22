package Server;
import java.net.*;
import java.io.*;
import Game.*;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #1
 * @student Id 7242530
 * @since Oct 6th , 2024
 */

public class ConnectionThread extends Thread{
    private Socket client1,client2;
    private Game g;

    public ConnectionThread(Socket c1, Socket c2) {
        client1 = c1;
        client2 = c2;

        g = new Game();
        g.start();
    }

    public void run() {
        try (
                ObjectOutputStream out1 = new ObjectOutputStream (client1.getOutputStream());
                ObjectOutputStream out2 = new ObjectOutputStream (client2.getOutputStream());
                ObjectInputStream in1 = new ObjectInputStream(client1.getInputStream());
                ObjectInputStream in2 = new ObjectInputStream(client2.getInputStream());
        ) {

            handleInput(in1,1);
            handleInput(in2,2);

            handleOutput(out1, out2);

        } catch (IOException e) {
            System.out.println("Connection TERMINATED");
        }
        System.out.println("ended");
    }


    private void handleInput(ObjectInputStream in, int player){

        new Thread(()->{
            while (true) {
                try {
                    GameData gd = (GameData) in.readObject();
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
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    private void handleOutput(ObjectOutputStream out1, ObjectOutputStream out2){
        try {
            while (true) {
                synchronized (g) {
                    if (g.isWriteReadyP1()) {
                        out1.writeObject(g.getP1Write());
                        out1.flush();
                    }

                    if (g.isWriteReadyP2()) {
                        out2.writeObject(g.getP2Write());
                        out2.flush();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Connection TERMINATED");
        }
    }


    /**
     * Turn object into array of bytes
     * These bytes are then send or reviewed
     * @param data
     * @return
     */
    private byte[] serialize(GameData data){
        try{
            ByteArrayOutputStream byteStr = new ByteArrayOutputStream();
            ObjectOutputStream objStr = new ObjectOutputStream(byteStr);
            objStr.writeObject(data);
            objStr.flush();
            return byteStr.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * turn stream of bytes into an object
     * @param data
     * @return
     */
    private GameData deSerialize(byte[] data){
        try {
            ByteArrayInputStream byteStr = new ByteArrayInputStream(data);
            ObjectInputStream objStr = new ObjectInputStream(byteStr);
            GameData gd = (GameData) objStr.readObject();
            return gd;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }



}
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


            new Thread(()->{
                while (true) {
                    try {
                        GameData gd = (GameData) in1.readObject();
                        System.out.println("read in 1");
                        g.insertP1Read(gd);

                    } catch (IOException e) {
                        System.out.println("player 1 closed connection");
                        synchronized (g) {
                            g.kill();
                        }
                        break; // empty or null object end of file
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            new Thread(()->{
                while (true) {
                    try {
                        GameData gd = (GameData) in2.readObject();
                        System.out.println("read in 2");
                        g.insertP2Read(gd);
                    } catch (IOException e) {
                        System.out.println("player 2 closed connection");
                        synchronized (g) {
                            g.kill();
                        }
                        break; // empty or null object end of file
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


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




}
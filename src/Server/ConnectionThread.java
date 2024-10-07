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
    private GameLogicHandler g;
    private boolean playerOne;

    public ConnectionThread(Socket c1, Socket c2) {
        client1 = c1;
        client2 = c2;
        g = new GameLogicHandler();
        playerOne = true;
    }

    public void run() {
        try (
                ObjectOutputStream out1 = new ObjectOutputStream (client1.getOutputStream());
                ObjectOutputStream out2 = new ObjectOutputStream (client2.getOutputStream());

                ObjectInputStream in1 = new ObjectInputStream(client1.getInputStream());
                ObjectInputStream in2 = new ObjectInputStream(client2.getInputStream());
        ) {
            boolean promptEnd = false;

            while (true) {

                // simple game control logic

                if(playerOne) {
                    // ----- player 1 turn ------
                    playerOne = false;
                    // send both users the game matrix and make it player 1's turn
                    out1.writeObject(new GameData(g.getGameMatrix(), 'X', -1, Status.TURN));
                    out1.flush();
                    out2.writeObject(new GameData(g.getGameMatrix(), 'O', -1, Status.WAITING));
                    out2.flush();

                    try{
                        Thread.sleep(10);
                    }catch (Exception e){}

                    // we now block this thread waiting for user 1's input
                    try {
                        GameData gd = (GameData)in1.readObject();
                        g.insert((gd.getMoveIndex()) % 3, (gd.getMoveIndex()) / 3, gd.getPlayerId());
                        System.out.println("inserted ");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }else{
                    // ----- player 2 turn ------
                    playerOne = true;
                    // send both users the game matrix and make it player 1's turn
                    out1.writeObject(new GameData(g.getGameMatrix(), 'X', -1, Status.WAITING));
                    out1.flush();
                    out2.writeObject(new GameData(g.getGameMatrix(), 'O', -1, Status.TURN));
                    out2.flush();

                    try{
                        Thread.sleep(10);
                    }catch (Exception e){}

                    g.insert(0,0,'X');

                    // we now block this thread waiting for user 2's input
                    try {
                        GameData gd = (GameData)in2.readObject();
                        g.insert(gd.getMoveIndex() % 3, gd.getMoveIndex() / 3, gd.getPlayerId());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                // check if play X wins
                if(g.win('X')){
                    out1.writeObject(new GameData(g.getGameMatrix(), 'X',-1,Status.WIN));
                    out2.writeObject(new GameData(g.getGameMatrix(), 'O',-1,Status.LOSE));
                    out1.flush();
                    out2.flush();
                    promptEnd = true;
                }

                // check if play O wins
                if(g.win('O')){
                    out1.writeObject(new GameData(g.getGameMatrix(), 'X',-1,Status.LOSE));
                    out2.writeObject(new GameData(g.getGameMatrix(), 'O',-1,Status.WIN));
                    out1.flush();
                    out2.flush();
                    promptEnd = true;
                }

                // check if there is a tie
                if(g.tieTester()){
                    out1.writeObject(new GameData(g.getGameMatrix(), 'X',-1,Status.TIE));
                    out2.writeObject(new GameData(g.getGameMatrix(), 'O',-1,Status.TIE));
                    out1.flush();
                    out2.flush();
                    promptEnd = true;
                }

                if(promptEnd){
                    promptEnd = false;

                    try{
                        Thread.sleep(1000);
                    }catch (Exception e){}

                    out1.writeObject(new GameData(g.getGameMatrix(), 'X',-1,Status.PROMPT));
                    out2.writeObject(new GameData(g.getGameMatrix(), 'O',-1,Status.PROMPT));
                    out1.flush();
                    out2.flush();

                    try{
                        Thread.sleep(10);
                    }catch (Exception e){}

                    // we now block this thread waiting for both users responses
                    try {
                        GameData gd = (GameData)in1.readObject();
                        GameData gd2 = (GameData)in2.readObject();

                        if(gd.getStatus() == Status.PLAYAGAIN && gd2.getStatus() == Status.PLAYAGAIN){
                            out1.writeObject(new GameData(g.getGameMatrix(), 'X',-1,Status.PLAYAGAIN));
                            out2.writeObject(new GameData(g.getGameMatrix(), 'O',-1,Status.PLAYAGAIN));
                            out1.flush();
                            out2.flush();
                            g.reset();
                        }else{
                            out1.writeObject(new GameData(g.getGameMatrix(), 'X',-1,Status.END));
                            out2.writeObject(new GameData(g.getGameMatrix(), 'O',-1,Status.END));
                            out1.flush();
                            out2.flush();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (IOException e) {
            System.out.println("Connection TERMINATED");
        }
    }
}
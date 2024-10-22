package Server;
import java.io.IOException;
import java.lang.Thread;
import java.util.LinkedList;
import java.util.Queue;

import Game.*;

public class Game extends Thread{

    private GameLogicHandler glh;
    private boolean playerOne;
    boolean GameLoop;

    Queue<GameData> writeBufferP1;
    Queue<GameData> writeBufferP2;

    Queue<GameData> readBufferP1;
    Queue<GameData> readBufferP2;

    public Game() {

        glh = new GameLogicHandler();
        playerOne = true;
        writeBufferP1 = new LinkedList<>();
        writeBufferP2 = new LinkedList<>();

        readBufferP1 = new LinkedList<>();
        readBufferP2 = new LinkedList<>();

        GameLoop = true;
    }

    public void kill(){
        GameLoop = false;
    }

    public void run() {
        loop();
    }

    private void loop(){
        boolean promptFlag = false;

        while (GameLoop) {

            // simple game control logic
            if(playerOne) {
                // ----- player 1 turn ------
                playerOne = false;
                // send both users the game matrix and make it player 1's turn

                writeBufferP1.add(new GameData(glh.getGameMatrix(), 'X', -1, Status.TURN));
                writeBufferP2.add(new GameData(glh.getGameMatrix(), 'O', -1, Status.WAITING));

                while (!isReadReadyP1()){ // wait for input
                   // try{
                   //     Thread.sleep(10);
                  //  }catch (Exception e){}
                }

                GameData gd = readBufferP1.poll();
                glh.insert((gd.getMoveIndex()) % 3, (gd.getMoveIndex()) / 3, gd.getPlayerId());
                System.out.println("inserted ");

            }else{
                // ----- player 2 turn ------
                playerOne = true;
                // send both users the game matrix and make it player 2's turn
                writeBufferP1.add(new GameData(glh.getGameMatrix(), 'X', -1, Status.WAITING));
                writeBufferP2.add(new GameData(glh.getGameMatrix(), 'O', -1, Status.TURN));

                while (!isReadReadyP2()){ // wait for input
                  //  try{
                //        Thread.sleep(10);
              //      }catch (Exception e){}
                }

                GameData gd = readBufferP2.poll();
                glh.insert((gd.getMoveIndex()) % 3, (gd.getMoveIndex()) / 3, gd.getPlayerId());
                System.out.println("inserted ");
            }

            // check if play X wins
            if(glh.win('X')){
                writeBufferP1.add(new GameData(glh.getGameMatrix(), 'X', -1, Status.WIN));
                writeBufferP2.add(new GameData(glh.getGameMatrix(), 'O', -1, Status.LOSE));
                promptFlag = true;
            }

            // check if play O wins
            if(glh.win('O')){
                writeBufferP1.add(new GameData(glh.getGameMatrix(), 'X', -1, Status.LOSE));
                writeBufferP2.add(new GameData(glh.getGameMatrix(), 'O', -1, Status.WIN));
                promptFlag = true;
            }

            // check if there is a tie
            if(glh.tieTester()){
                writeBufferP1.add(new GameData(glh.getGameMatrix(), 'X', -1, Status.TIE));
                writeBufferP2.add(new GameData(glh.getGameMatrix(), 'O', -1, Status.TIE));
                promptFlag = true;
            }

            if(promptFlag){
                promptFlag = false;

                try{
                    Thread.sleep(1000);
                }catch (Exception e){}

                writeBufferP1.add(new GameData(glh.getGameMatrix(), 'X', -1, Status.PROMPT));
                writeBufferP2.add(new GameData(glh.getGameMatrix(), 'O', -1, Status.PROMPT));

                while (!isReadReadyP2() || !isReadReadyP1()){ // wait for input from both users
                    //try{
                     //   Thread.sleep(10);
                    //}catch (Exception e){}
                   // System.out.println("waiting for prompt");
                }

                GameData gd = readBufferP1.poll();
                GameData gd2 = readBufferP2.poll();

                if(gd.getStatus() == Status.PLAYAGAIN && gd2.getStatus() == Status.PLAYAGAIN){
                    writeBufferP1.add(new GameData(glh.getGameMatrix(), 'X', -1, Status.PLAYAGAIN));
                    writeBufferP2.add(new GameData(glh.getGameMatrix(), 'O', -1, Status.PLAYAGAIN));
                    playerOne = true;
                    glh.reset();
                }else{
                    writeBufferP1.add(new GameData(glh.getGameMatrix(), 'X', -1, Status.END));
                    writeBufferP2.add(new GameData(glh.getGameMatrix(), 'O', -1, Status.END));
                    kill();
                    break;
                }
            }
        }
    }

    public synchronized boolean isReadReadyP1() {
        return readBufferP1.size() > 0;
    }
    public synchronized boolean isReadReadyP2() {
        return readBufferP2.size() > 0;
    }

    public synchronized boolean isWriteReadyP1() {
        return writeBufferP1.size() > 0;
    }
    public synchronized boolean isWriteReadyP2() {
        return writeBufferP2.size() > 0;
    }

    public synchronized GameData getP1Write() {
        return writeBufferP1.poll();
    }

    public synchronized GameData getP2Write() {
        return writeBufferP2.poll();
    }


    public synchronized void insertP1Read(GameData g) {
        readBufferP1.add(g);
    }

    public synchronized void insertP2Read(GameData g) {
        readBufferP2.add(g);
    }



}

package GUI;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #1
 * @student Id 7242530
 * @since Oct 6th , 2024
 */

import Game.GameData;
import Game.Status;

import java.net.*;
import java.io.*;
import java.lang.Thread;

public class Comms extends Thread{

    boolean canWrite;
    TicTacToe tacToe;

    public Comms(TicTacToe con){
        tacToe = con;
    }

    String hostName="localhost";
    int portNumber=1080;

    public void run() {

        canWrite = false;

        try (
                Socket conn = new Socket(hostName, portNumber);

                ObjectOutputStream sockOut = new ObjectOutputStream (conn.getOutputStream());
                ObjectInputStream sockIn = new ObjectInputStream(conn.getInputStream());
        ) {
            while (true) {

                if(!canWrite) {

                    try {
                        GameData fromServer = (GameData)sockIn.readObject();

                        if(fromServer == null){
                            break;
                        }
                        parseInputStream(fromServer);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                try{
                    Thread.sleep(10);
                }catch (Exception e){}

                if(canWrite && tacToe.isDataReady()){
                    canWrite = false;
                    sockOut.writeObject(tacToe.getSendToServer());
                    sockOut.flush();
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("problem with the host name.");
        } catch (IOException e) {
            System.out.println("IO error for the connection.");
        }
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
        tacToe.setMatrix(in.getMatrix());

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

}

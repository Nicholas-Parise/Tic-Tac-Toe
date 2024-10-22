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
import Game.GameLogicHandler;
import Game.Status;

import java.util.LinkedList;
import java.util.Queue;

public class TicTacToe {

    GUI gui;
    GameLogicHandler glh;

    boolean canSend;
    boolean promptPlayAgain;
    boolean GameLoop;
    char player;
    Status gameState;
    int ghosti, ghostj;

    Queue<GameData> WriteBuffer;

    public TicTacToe(){

        canSend = false;
        promptPlayAgain = false;
        GameLoop = true;
        gameState = Status.WAITING;
        WriteBuffer = new LinkedList<>();

        glh = new GameLogicHandler();

        setPlayer('O');

        gui = new GUI(this);
        gui.setVisible(true);

        Comms c = new Comms(this);
        c.start();

        while (GameLoop){
            gui.update();
        }
        gui.close();
        c.kill();
        c.interrupt();
    }

    /**
     * sets the player and the ghost player to be the correct char
     * @param player
     */
    public void setPlayer(char player) {
        this.player = player;
    }

    /**
     * resets ghost position to be hidden
     */
    public void removeGhost(){
        ghosti = -1;
        ghostj = -1;
    }

    /**
     * inserts the ghost reusing the code to insert players.
     *
     * @param col column to add ghost
     * @return true if can insert
     */
    public boolean insertGhost(int col,int row){

        if (!glh.canInsert(col,row)){
            return false;
        }
            ghosti = row;
            ghostj = col;
        return true;
    }


    /**
     * code to play again
     */
    public void PlayAgain(){
        Status tempStatus = gui.SpawnPlayAgainBox();

        if(tempStatus == Status.END){
            GameLoop = false;
        }

        WriteBuffer.add(new GameData(glh.getGameMatrix(), player, 0, tempStatus));

        promptPlayAgain = false;
        canSend = false;
        System.out.println(tempStatus+" <- sent to server");
    }

    /**
     * Code to insert and send to server a player into a column
     * @param index of button
     */
    public void insertAt(int index){

        WriteBuffer.add(new GameData(glh.getGameMatrix(), player, index, Status.RESPONSE));

        canSend = false;
        System.out.println(index+" <- sent to server");
    }


    public GameData getSendToServer() {
        return WriteBuffer.poll();
    }

    public boolean isDataReady() {
        return WriteBuffer.size() > 0;
    }

    public boolean isCanSend(){
        return canSend;
    }

    public void setCanSend(boolean canSend) {   // means console now waiting for commands.
        this.canSend = canSend;
    }

    public boolean getPromptPlayAgain() {
        return promptPlayAgain;
    }

    public void setPromptPlayAgain(boolean promptPlayAgain) {
        this.promptPlayAgain = promptPlayAgain;
    }

    public void setGameState(Status s) {
        gameState = s;
    }

    public Status getGameState() {
        return gameState;
    }

    public static void main(String[] args) {new TicTacToe();}
}

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

import java.util.LinkedList;
import java.util.Queue;

public class TicTacToe {

    GUI gui;

    char[][] matrix;
    boolean canSend;
    boolean promptPlayAgain;
    boolean GameLoop;
    char player;
    char ghostPlayer;
    Status gameState;
    int ghosti, ghostj;

    Queue<GameData> WriteBuffer;

    public TicTacToe(){

        matrix = new char[3][3];
        canSend = false;
        promptPlayAgain = false;
        GameLoop = true;
        gameState = Status.WAITING;
        WriteBuffer = new LinkedList<>();

        removeGhost();
        createMatrix();

        setPlayer('O');

        gui = new GUI(this);
        gui.setVisible(true);

        new Comms(this).start();

        while (GameLoop){
            gui.update();
        }
        gui.close();
    }

    /**
     * sets the player and the ghost player to be the correct char
     * @param player
     */
    public void setPlayer(char player) {
        this.player = player;
        ghostPlayer = Character.toLowerCase(player);
    }

    /**
     * resets ghost position to be hidden
     */
    public void removeGhost(){
            matrix[ghosti][ghostj] = '*';
    }

    /**
     * inserts the ghost reusing the code to insert players.
     *
     * @param col column to add ghost
     * @return true if can insert
     */
    public boolean insertGhost(int col,int row){

        if (!canInsert(col,row)){
            return false;
        }
        if(matrix[row][col] == '*'){
            matrix[row][col] = ghostPlayer;
            ghosti = row;
            ghostj = col;
        }
        return true;
    }

    /**
     *
     * @param col
     * @return true if can insert in column
     */
    public boolean canInsert(int col,int row){
        if (matrix[row][col] != 'X' && matrix[row][col] != 'O') {
            return true;
        }
        return false;
    }

    /**
     * code to play again
     */
    public void PlayAgain(){
        Status tempStatus = gui.SpawnPlayAgainBox();

        if(tempStatus == Status.END){
            GameLoop = false;
        }

        WriteBuffer.add(new GameData(getMatrix(), player, 0, tempStatus));

        promptPlayAgain = false;
        canSend = false;
        System.out.println(tempStatus+" <- sent to server");
    }

    /**
     * Code to insert and send to server a player into a column
     * @param index of button
     */
    public void insertAt(int index){

        WriteBuffer.add(new GameData(getMatrix(), player, index, Status.RESPONSE));

        canSend = false;
        System.out.println(index+" <- sent to server");
    }

    /**
     * just to prevent null pointers, adds * to all the positions in the matrix
     */
    public void createMatrix(){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                matrix[i][j] = '*';
            }
        }
    }


    public GameData getSendToServer() {
        return WriteBuffer.poll();
    }

    public synchronized char[][] getMatrix() {
        return matrix;
    }

    public synchronized void setMatrix(char[][] m) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                matrix[i][j] = m[i][j];
            }
        }
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

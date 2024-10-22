package Game;

import java.io.*;

public class GameData implements Serializable {

    private static final long serialVersionUID = 1L;
    private char[][] gameMatrix; // Represents the Tic-Tac-Toe board
    private char playerId;    // The player who made the move
    private int moveIndex;   // The index on the board where the move was made
    private Status status;  // used to determine the state of the game

    public GameData(char[][] gameMatrix, char playerId, int moveIndex, Status status) {
        this.gameMatrix = new char[3][3];
        this.playerId = playerId;
        this.moveIndex = moveIndex;
        this.status = status;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.gameMatrix[i][j] = gameMatrix[i][j];
            }
        }
    }

    public char[][] getMatrix() {
        return gameMatrix;
    }

    public char getPlayerId() {
        return playerId;
    }

    public int getMoveIndex() {
        return moveIndex;
    }

    public Status getStatus() {
        return status;
    }


    /**
     * Turn object into array of bytes
     * These bytes are then send or reviewed
     * @param data
     * @return
     */
    public static byte[] serialize(GameData data){
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
    public static GameData deSerialize(byte[] data){
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
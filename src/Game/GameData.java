package Game;

import java.io.Serializable;

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
}
package Game;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #1
 * @student Id 7242530
 * @since Oct 6th , 2024
 */

public class GameLogicHandler {

    public char[][] gameMatrix;
    // blank, p1, p2
    // *, X, O
    public GameLogicHandler(){
        gameMatrix = new char[3][3];
        reset();
        //gameMatrix[0][1] = 'X';
    }

    /**
     * simple init for game matrix
     */
    public void reset(){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gameMatrix[i][j] = '*';
            }
        }
    }


    public synchronized char[][] getGameMatrix(){
        return gameMatrix;
    }


    /**
     *
     * @param col which column to insert
     * @return false if cannot insert at col number
     */
    public synchronized boolean insert(int col, int row, char pl){

        if (!canInsert(col,row)){
            return false;
        }
        gameMatrix[row][col] = pl;

        return true;
    }


    /**
     *
     * @return true if every spot is full
     */
    public boolean tieTester() {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(canInsert(i,j)){
                    return false;
                }
            }
        }
        return true;
    }


    /**
     *
     * @param col column of matrix
     * @return true if can insert
     */
    public boolean canInsert(int col, int row){
        if (gameMatrix[row][col] == '*') {
            return true;
        }
        return false;
    }


    /** Solver to see if 3 are connected
     * Since order doesn't matter, and we start in the top left corner
     * only four directions need to be checked
     * vertical, horizontal and both diagonals.
     *
     * @return
     */
    public boolean win(char pl){

        for (int i = 0; i < 3; i++) {
            if(gameMatrix[0][i] == pl && gameMatrix[1][i] == pl && gameMatrix[2][i] == pl){ // horizontal
                return true;
            }
            if(gameMatrix[i][0] == pl && gameMatrix[i][1] == pl && gameMatrix[i][2] == pl){ // horizontal
                return true;
            }
        }

        if(gameMatrix[0][0] == pl && gameMatrix[1][1] == pl && gameMatrix[2][2] == pl){
            return true;
        }

        if(gameMatrix[0][2] == pl && gameMatrix[1][1] == pl && gameMatrix[2][0] == pl){
            return true;
        }


        return false;
    }


}

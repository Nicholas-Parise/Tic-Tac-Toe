package GUI;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #1
 * @student Id 7242530
 * @since Oct 6th , 2024
 */


import Game.Status;

import javax.swing.*;
import java.awt.*;

class Render extends JPanel {

    TicTacToe tacToe;

    public Render(TicTacToe ttt){
        tacToe = ttt;
    }

    public void paint(Graphics g){
        paintComponents(g);
    }

    @Override
    public void paintComponents(Graphics g) {
        super.paintComponent(g);
        makeGrid(g);
        drawText(g);
    }

    /**
     * add the text to the window
     * @param g
     */
    public void drawText(Graphics g){
        Graphics2D g2d = (Graphics2D) g;

        if(tacToe.getGameState() == Status.WIN) {

            float f=80.0f; // font size.
            g2d.setFont(g.getFont().deriveFont(f));
            g2d.setColor(Color.green);
            g2d.drawString("Winner",40,f+110);

        }else if(tacToe.getGameState() == Status.LOSE) {

            float f=80.0f; // font size.
            g2d.setFont(g.getFont().deriveFont(f));
            g2d.setColor(Color.red);
            g2d.drawString("Loser",80,f+110);

        }else if(tacToe.getGameState() == Status.WAITING) {

            float f=80.0f; // font size.
            g2d.setFont(g.getFont().deriveFont(f));
            g2d.setColor(Color.red);
            g2d.drawString("Waiting",20,f+110);

        }
    }

    /**
     * make the grid and determine the colours
     * @param g
     */
    public void makeGrid(Graphics g){

        Graphics2D g2d = (Graphics2D) g;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                g2d.setColor(Color.gray);

                if(tacToe.glh.getGameMatrix()[i][j] == 'X') {
                    g2d.setColor(Color.red);
                }else if(tacToe.glh.getGameMatrix()[i][j] == 'O'){
                    g2d.setColor(Color.yellow);
                }

                if(tacToe.ghosti == i && tacToe.ghostj == j){
                    if(tacToe.player == 'X'){
                        g2d.setColor(Color.PINK);
                    }else{
                        g2d.setColor(Color.ORANGE);
                    }
                }
                g.fillOval(20+j*90, 30+i*95, 85, 85);
            }
        }
    }
}

public class GUI extends JFrame {

    final static int FRAMEWIDTH = 320;
    final static int FRAMEHEIGHT = 360;

    JButton[] b;

    TicTacToe tacToe;

    public GUI(TicTacToe connectFour) {

        tacToe = connectFour;

        addButtons();

        add(new Render(tacToe));

        setSize(FRAMEWIDTH, FRAMEHEIGHT);
        setTitle("Tic-Tac-Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        //setLayout(null);

    }

    public void close(){
        dispose();
    }

    public void update() {
        // updates the render window
        validate();
        repaint();
    }

    /**
     * spawn the play again box to determine
     * @return status message of what user clicked
     */
    public Status SpawnPlayAgainBox(){
        String message = "Would you like to play again?";
        int answer = JOptionPane.showConfirmDialog(this, message);
        if (answer == JOptionPane.YES_OPTION) {
            return Status.PLAYAGAIN;
        } else if (answer == JOptionPane.NO_OPTION) {
            return Status.END;
        }
        return Status.END;
    }


    /**
     * add the buttons to the GUI
     */
    private void addButtons(){

        b = new JButton[9];
        int temp = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                b[temp] = new JButton("" + (temp + 1));//creating instance of JButton
                b[temp].setBounds(38+j*90, 20+i*95, 50, 15);

                add(b[temp]);

                final int row = i;
                final int col = j;
                final int index = temp;

                b[temp].addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseExited(java.awt.event.MouseEvent evt) { tacToe.removeGhost(); }
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        tacToe.insertGhost(col,row);
                    }

                    public void mouseClicked(java.awt.event.MouseEvent evt) {

                        if(tacToe.glh.canInsert(col,row)&& tacToe.isCanSend() && !tacToe.getPromptPlayAgain()) {
                            tacToe.insertAt(index);
                         }
                        System.out.println(col+" "+row);
                    }
                });
                temp++;
            }
        }
    }


}

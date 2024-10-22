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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class UDPComms extends Thread{

    boolean canWrite;
    TicTacToe tacToe;
    boolean GameLoop;

    String hostName="localhost";
    int portNumber = 1080;

    DatagramSocket socket;
    InetAddress serverAddr;

    public UDPComms(TicTacToe t) {
        tacToe = t;
        GameLoop = true;
        canWrite = false;
        try {
            socket = new DatagramSocket();
            serverAddr = InetAddress.getByName(hostName);
        }catch (SocketException | UnknownHostException e){
            System.out.println("could not establish connection");
            GameLoop = false;
        }
    }

    public void kill(){
        GameLoop = false;
    }

    public void run() {

        handleOutput();

        handleInput();

        tacToe.kill();
    }


    private void handleOutput(){
        new Thread(()->{
            try {
                while (true) {
                    if (canWrite && tacToe.isDataReady()) {
                        System.out.println("is going to send data");
                        canWrite = false;
                        byte[] data = GameData.serialize(tacToe.getSendToServer());
                        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, portNumber);
                        socket.send(packet);
                        System.out.println("finished sending data");
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection TERMINATED");
                tacToe.kill();
            }
        }).start();
    }


    private void handleInput(){

        byte[] buffer = new byte[1024];

        while (GameLoop) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                GameData gd = GameData.deSerialize(packet.getData());
                parseInputStream(gd);
                portNumber = packet.getPort();  // this is important since the server will change its port number after getting established.
                System.out.println("read in from server "+portNumber);
            } catch (IOException e) {
                System.out.println("server closed the connection");
                tacToe.kill();
                break;
            }
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
        tacToe.glh.setMatrix(in.getMatrix());

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

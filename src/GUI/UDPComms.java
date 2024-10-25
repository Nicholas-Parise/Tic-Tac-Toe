package GUI;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #2
 * @student Id 7242530
 * @since Oct 23th , 2024
 */

import Game.GameData;
import Transport.MessageType;
import Transport.Segment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class UDPComms extends Thread{

    boolean canWrite;
    TicTacToe tacToe;

    volatile boolean GameLoop;
    volatile int portNumber = 1080;
    String hostName="localhost";

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

    public synchronized void kill(){
        GameLoop = false;
    }

    public void run() {

        handshake();

        handleOutput();

        handleInput();

        tacToe.kill();
        closeConnection();
    }


    private void closeConnection(){

        try {
            byte[] data = Segment.serialize(new Segment(MessageType.FIN, 0, 0, null));
            DatagramPacket finPacket = new DatagramPacket(data, data.length, serverAddr, portNumber);
            socket.send(finPacket);
            System.out.println("FIN sent to the server");
        }catch (IOException e) {}
    }

    private void handshake(){

            try {
                Segment syn = new Segment(MessageType.SYN, 0, 0, null);
                byte[] data = Segment.serialize(syn);
                DatagramPacket synPacket = new DatagramPacket(data, data.length, serverAddr, portNumber);
                socket.send(synPacket);
                System.out.println("SYN sent to the server");


                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                Segment s = Segment.deSerialize(packet.getData());

                if(s.getMessageType() == MessageType.SYN_ACK){
                    System.out.println("get SYNACK");
                    Segment Ack = new Segment(MessageType.ACK, s.getSequenceNumber(), s.getSequenceNumber(), null);
                    data = Segment.serialize(Ack);
                    DatagramPacket ackPacket = new DatagramPacket(data, data.length, serverAddr, portNumber);
                    socket.send(ackPacket);
                    System.out.println("ACK sent to the server");
                }

                System.out.println("read in from server "+portNumber);
            } catch (IOException e) {
                System.out.println("failed to establish connection with server");
                tacToe.kill();
            }
    }



    private void handleOutput(){
        new Thread(()->{
            try {
                while (GameLoop) {

                    try{
                        Thread.sleep(50);
                    }catch (Exception e){}

                    synchronized (tacToe) {
                        if (canWrite && tacToe.isDataReady()) {
                            System.out.println("is going to send data");
                            setCanWrite(false);
                            byte[] data = Segment.serialize(new Segment(MessageType.DATA,1,1,tacToe.getSendToServer()));
                            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, portNumber);
                            socket.send(packet);
                            System.out.println("finished sending data");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection TERMINATED");
                tacToe.kill();
                kill();
            }
        }).start();
    }


    private void handleInput(){

        byte[] buffer = new byte[1024];

        while (GameLoop) {

            try{
                Thread.sleep(50);
            }catch (Exception e){}

            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                Segment s = Segment.deSerialize(packet.getData());
                System.out.println("read in from server "+portNumber);

                if(s.getMessageType() == MessageType.DATA) {
                    parseInputStream(s.getGameData());
                    portNumber = packet.getPort();  // this is important since the server will change its port number after getting established.
                }else if (s.getMessageType() == MessageType.FIN){
                    System.out.println("server closed the connection");
                    tacToe.kill();
                }

            } catch (IOException e) {
                System.out.println("server closed the connection");
                tacToe.kill();
                break;
            }
        }
    }


    public synchronized void setCanWrite(boolean canWrite) {
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

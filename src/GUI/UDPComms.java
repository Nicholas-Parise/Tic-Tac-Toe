package GUI;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #2
 * @student Id 7242530
 * @since Oct 25th , 2024
 */

import Game.GameData;
import Transport.MessageType;
import Transport.Segment;
import Transport.SentPacket;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;

public class UDPComms extends Thread{

    boolean canWrite;
    TicTacToe tacToe;

    volatile boolean gameLoop;

    String hostName="localhost";
    DatagramSocket socket;
    InetAddress serverAddr;
    volatile int serverPort = 1080;

    private volatile Queue<SentPacket> retransmit;
    private final int MAX_TIMEOUT = 500;
    private int sequence;
    private int expectedSequence;

    public UDPComms(TicTacToe t) {
        tacToe = t;
        gameLoop = true;
        canWrite = false;
        retransmit = new LinkedList<>();
        sequence = 0;
        expectedSequence = 0;
        try {
            socket = new DatagramSocket();
            serverAddr = InetAddress.getByName(hostName);
        }catch (SocketException | UnknownHostException e){
            System.out.println("could not establish connection");
            gameLoop = false;
        }
    }

    public void run() {

        handshake();

        handleRetransmit();

        handleOutput();

        handleInput();

        tacToe.kill();
        kill();
    }


    public synchronized void kill(){
        gameLoop = false;
        closeConnection();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        socket.close();
    }


    /**
     * close the connection with the server
     */
    private void closeConnection(){

        try {
            System.out.println("FIN sent to the server");
            byte[] data = Segment.serialize(new Segment(MessageType.FIN, 0, 0, null));
            DatagramPacket finPacket = new DatagramPacket(data, data.length, serverAddr, serverPort);
            socket.send(finPacket);
        }catch (IOException e) {}
    }

    /**
     * perform the TCP 3 way handshake
     */
    private void handshake(){

            try {

                Segment syn = new Segment(MessageType.SYN, 0, 0, null);
                byte[] data = Segment.serialize(syn);
                DatagramPacket synPacket = new DatagramPacket(data, data.length, serverAddr, serverPort);
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
                    DatagramPacket ackPacket = new DatagramPacket(data, data.length, serverAddr, serverPort);
                    socket.send(ackPacket);
                    System.out.println("ACK sent to the server");
                }

                System.out.println("read in from server "+ serverPort);
            } catch (IOException e) {
                System.out.println("failed to establish connection with server");
                tacToe.kill();
            }
    }


    /**
     * Handle all output in its own thread,
     * if there is any data in the buffer send it
     */
    private void handleOutput(){
        new Thread(()->{
            try {
                while (gameLoop) {

                    synchronized (tacToe) {
                        if (canWrite && tacToe.isDataReady()) {
                            System.out.println("is going to send data");
                            setCanWrite(false);
                            byte[] data = Segment.serialize(new Segment(MessageType.DATA,sequence,1,tacToe.getSendToServer()));
                            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, serverPort);
                            socket.send(packet);
                            sequence++;
                            System.out.println("finished sending data");
                        }
                    }
                    Thread.sleep(50);
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Connection TERMINATED");
                tacToe.kill();
                kill();
            }
        }).start();
    }

    /**
     * handles all input from the server
     * addes data to proper places and sets necessary flags
     */
    private void handleInput(){

        byte[] buffer = new byte[1024];

        while (gameLoop) {

            try{
                Thread.sleep(50);
            }catch (Exception e){}

            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                Segment s = Segment.deSerialize(packet.getData());
                System.out.println("read in from server "+ serverPort);

                if(s.getSequenceNumber() != expectedSequence) {
                    // disregard out of order packet.
                    System.out.println("out of order packet, disregarding");
                }else{

                    if(s.getMessageType() == MessageType.DATA) {
                        parseInputStream(s.getGameData());
                        serverPort = packet.getPort();  // this is important since the server will change its port number after getting established.
                        sendACK(s);
                        expectedSequence++;
                    }else if (s.getMessageType() == MessageType.FIN){
                        System.out.println("server closed the connection");
                        tacToe.kill();
                        kill();
                    }else if(s.getMessageType() == MessageType.ACK) {
                        int ack = s.getAcknowledgmentNumber();
                        synchronized (retransmit) {
                            retransmit.removeIf(p -> p.getSequence() == ack - 1);
                        }
                    }

                }


            } catch (IOException e) {
                System.out.println("server closed the connection");
                tacToe.kill();
                break;
            }
        }
    }

    /**
     * resend packets that haven't been acknowledged
     * If time is over the timeout we resend it
     */
    private void handleRetransmit(){
        new Thread(()->{

            while (gameLoop) {
                try {
                    synchronized (retransmit) {
                        if (!retransmit.isEmpty()) {
                            // for (SentPacket sp : retransmit) {
                            SentPacket sp = retransmit.peek();
                            if (System.currentTimeMillis() - sp.getTime() > MAX_TIMEOUT) {
                                socket.send(sp.getPacket());
                                sp.resetTime();
                            }
                        }
                    }
                    //}
                    Thread.sleep(150);

                } catch (IOException | InterruptedException e) {
                    System.out.println("Connection TERMINATED");
                }
            }
        }).start();
    }

    /**
     * ACK the recieved datagram, update the acknowledgement number accordingly
     * @param s Segment just received
     * @throws IOException
     */
    private void sendACK(Segment s) throws IOException {
        byte[] ackData = Segment.serialize(new Segment(MessageType.ACK, 0, s.getSequenceNumber() + 1, null));
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, serverAddr, serverPort);
        socket.send(ackPacket);
        System.out.println("ACK sent to the server");
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

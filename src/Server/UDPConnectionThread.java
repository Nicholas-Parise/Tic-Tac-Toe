package Server;

import GUI.UDPComms;
import Game.GameData;
import Transport.MessageType;
import Transport.Segment;
import Transport.SentPacket;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #2
 * @student Id 7242530
 * @since Oct 22nd , 2024
 */

public class UDPConnectionThread extends Thread{

    private DatagramSocket serverSocket;
    private DatagramPacket client1,client2;
    private final int client1Port, client2Port;
    private final InetAddress client1Addr, client2Addr;
    private final int gamePort;
    private int client1sequence;
    private int client2sequence;

    private Game g;

    private volatile Queue<SentPacket> retransmit;
    private final int MAX_TIMEOUT = 500;

    private volatile boolean gameLoop;

    public UDPConnectionThread(int gamePort,DatagramPacket c1, DatagramPacket c2) throws SocketException {

        this.client1 = c1;
        this.client2 = c2;
        this.gamePort = gamePort;
        this.serverSocket = new DatagramSocket(gamePort);

        client1Addr = client1.getAddress();
        client1Port = client1.getPort();

        client2Addr = client2.getAddress();
        client2Port = client2.getPort();

        retransmit = new LinkedList<>();

        client1sequence = 0;
        client2sequence = 0;

        gameLoop = true;

        g = new Game();
        g.start();
    }

    public void run() {

        System.out.println("New connection thread listening on port: "+gamePort);

        handleRetransmit();

        handleOutput();

        handleInput();

        serverSocket.close();
        System.out.println("Game thread terminated");
    }


    /**
     * handle all input from the clients.
     * We detect which player its from and send it to the correct buffer
     */
    private void handleInput(){

        byte[] buffer = new byte[1024];

        while (gameLoop) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                serverSocket.receive(packet);
                Segment s = Segment.deSerialize(packet.getData());

                if(s.getMessageType() == MessageType.FIN) {
                    System.out.println("received FIN closing connections...");
                    sendFIN(client1Addr,client1Port,client1sequence);
                    sendFIN(client2Addr,client2Port,client2sequence);
                    kill();
                    synchronized (g) {
                        g.kill();
                    }
                }else if(s.getMessageType() == MessageType.ACK) {
                    System.out.println("received an ACK");
                    int ack = s.getAcknowledgmentNumber();
                    synchronized (retransmit) {
                        retransmit.removeIf(p -> p.getSequence() == ack - 1);
                    }

                }else if(s.getMessageType() == MessageType.DATA) {

                    if(packet.getAddress().equals(client1Addr) && packet.getPort() == client1Port) {
                        System.out.print("received DATA from player 1");
                        g.insertP1Read(s.getGameData());
                        sendACK(packet, s);
                    }else{
                        System.out.print("received DATA from player 2");
                        g.insertP2Read(s.getGameData());
                        sendACK(packet, s);
                    }
                }

            } catch (IOException e) {
                System.out.println("a player closed the connection");
                synchronized (g) {
                    g.kill();
                }
                break;
            }
        }
    }


    private void handleOutput(){
        new Thread(()->{
            try {
                while (gameLoop) {
                    synchronized (g) {

                        if (g.isWriteReadyP1()) {
                            Segment s = new Segment(MessageType.DATA,client1sequence,0,g.getP1Write());
                            byte[] data = Segment.serialize(s);
                            DatagramPacket packet = new DatagramPacket(data, data.length, client1Addr, client1Port);
                            synchronized (retransmit) {
                                retransmit.add(new SentPacket(packet, s.getSequenceNumber()));
                            }
                            serverSocket.send(packet);
                            System.out.println("sent player 1 data");
                            client1sequence++;
                        }

                        if (g.isWriteReadyP2()) {
                            Segment s = new Segment(MessageType.DATA,client2sequence,1,g.getP2Write());
                            byte[] data = Segment.serialize(s);
                            DatagramPacket packet = new DatagramPacket(data, data.length, client2Addr, client2Port);
                            synchronized (retransmit) {
                                retransmit.add(new SentPacket(packet, s.getSequenceNumber()));
                            }
                            serverSocket.send(packet);
                            System.out.println("sent player 2 data");
                            client2sequence++;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection TERMINATED");
            }
        }).start();
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
                        if(!retransmit.isEmpty()) {
                            SentPacket sp = retransmit.peek();
                            //for (SentPacket sp : retransmit) {
                            if (System.currentTimeMillis() - sp.getTime() > MAX_TIMEOUT) {
                                System.out.println("retransmitting");
                                serverSocket.send(sp.getPacket());
                                sp.resetTime();
                            }
                            //}
                        }
                    }
                    // to not bog down the thread and to lighten retransmission on the system
                    Thread.sleep(150);

                } catch (IOException | InterruptedException e) {
                    System.out.println("Connection TERMINATED");
                }
            }
        }).start();
    }

    /**
     * send an ACK to the client
     * @param packet the datagram received from the client
     * @param s the segment recieved from the client
     * @throws IOException
     */
    private void sendACK(DatagramPacket packet, Segment s) throws IOException {
        byte[] ackData = Segment.serialize(new Segment(MessageType.ACK, 0, s.getSequenceNumber() + 1, null));
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, packet.getAddress(), packet.getPort());
        serverSocket.send(ackPacket);
    }


    /**
     * send a FIN message to the client to end the session
     * @param address
     * @param port
     * @throws IOException
     */
    private void sendFIN(InetAddress address, int port, int sequence) throws IOException {
        byte[] finData = Segment.serialize(new Segment(MessageType.FIN, sequence,  1, null));
        DatagramPacket finPacket = new DatagramPacket(finData, finData.length, address, port);
        serverSocket.send(finPacket);
    }

    /**
     * kill the game thread by disabling all the threads
     */
    private synchronized void kill(){
        gameLoop = false;
    }



}
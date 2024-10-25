package Server;

import Game.GameData;
import Transport.MessageType;
import Transport.Segment;

import java.io.*;
import java.net.*;

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

    private Game g;

    public UDPConnectionThread(int gamePort,DatagramPacket c1, DatagramPacket c2) throws SocketException {

        this.client1 = c1;
        this.client2 = c2;
        this.gamePort = gamePort;
        this.serverSocket = new DatagramSocket(gamePort);

        client1Addr = client1.getAddress();
        client1Port = client1.getPort();

        client2Addr = client2.getAddress();
        client2Port = client2.getPort();

        g = new Game();
        g.start();
    }

    public void run() {

        System.out.println("New connection thread listening on port: "+gamePort);

        handleOutput();

        handleInput();

        serverSocket.close();
        System.out.println("ended");
    }


    /**
     * handle all input from the clients.
     * We detect which player its from and send it to the correct buffer
     */
    private void handleInput(){

        byte[] buffer = new byte[1024];

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                serverSocket.receive(packet);

                System.out.print("received a packet from ");

                if(packet.getAddress().equals(client1Addr) && packet.getPort() == client1Port){
                    System.out.print("client 1 ");

                    Segment s = Segment.deSerialize(packet.getData());
                    g.insertP1Read(s.getGameData());
                    System.out.println("and the read was successful");
                }else{
                    System.out.print("client 2 ");
                    Segment s = Segment.deSerialize(packet.getData());
                    g.insertP2Read(s.getGameData());
                    System.out.println("and the read was successful");
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
                while (true) {
                    synchronized (g) {
                        if (g.isWriteReadyP1()) {
                            byte[] data = Segment.serialize(new Segment(MessageType.DATA,1,1,g.getP1Write()));
                            DatagramPacket packet = new DatagramPacket(data, data.length, client1Addr, client1Port);
                            serverSocket.send(packet);
                            System.out.println("sent player 1 data");
                        }

                        if (g.isWriteReadyP2()) {

                            byte[] data = Segment.serialize(new Segment(MessageType.DATA,1,1,g.getP2Write()));
                            DatagramPacket packet = new DatagramPacket(data, data.length, client2Addr, client2Port);
                            serverSocket.send(packet);
                            System.out.println("sent player 2 data");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection TERMINATED");
            }
        }).start();
    }






}
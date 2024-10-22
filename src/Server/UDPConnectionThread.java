package Server;

import Game.GameData;

import java.io.*;
import java.net.*;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #1
 * @student Id 7242530
 * @since Oct 6th , 2024
 */

public class UDPConnectionThread extends Thread{

    private DatagramSocket serverSocket;
    private DatagramPacket client1,client2;
    private int client1Port, client2Port;
    private InetAddress client1Addr, client2Addr;
    private Game g;
    private int gamePort;

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

        System.out.println("ended");
    }


    private void handleInput(){

        byte[] buffer = new byte[1024];

        while (true) {
            System.out.println("test");
            try {
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                serverSocket.receive(packet);

                System.out.println("received packet");

                if(packet.getAddress().equals(client1Addr) && packet.getPort() == client1Port){
                    GameData gd = GameData.deSerialize(packet.getData());
                    g.insertP1Read(gd);
                    System.out.println("read in 1");
                }else{
                    GameData gd = GameData.deSerialize(packet.getData());
                    g.insertP2Read(gd);
                    System.out.println("read in 2");
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
                            byte[] data = GameData.serialize(g.getP1Write());
                            DatagramPacket packet = new DatagramPacket(data, data.length, client1Addr, client1Port);
                            serverSocket.send(packet);
                            System.out.println("sent player 1 data");
                        }

                        if (g.isWriteReadyP2()) {

                            byte[] data = GameData.serialize(g.getP2Write());
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
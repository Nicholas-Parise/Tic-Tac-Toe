package Server;

import Transport.MessageType;
import Transport.Segment;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #2
 * @student Id 7242530
 * @since Oct 24th , 2024
 */

public class UDPThreadedServer {

    private int gamePort;
    DatagramSocket serverSocket;
    DatagramPacket client1;
    DatagramPacket client2;

    /**
     * runs in a loop forever to try and connect clients
     * if a client fails to connect we retry.
     * we retry until we get 2 clients and then we spin up a new thread with a new port
     * @param portNumber
     */
    public UDPThreadedServer(int portNumber){

        gamePort = portNumber+1;
        try {
            serverSocket = new DatagramSocket (portNumber);

            while (true) {

                do{
                    client1 = handshake();
                }while(client1 == null);

                do{
                    client2 = handshake();
                }while (client2 == null);

                new UDPConnectionThread(gamePort,client1,client2).start();
                gamePort++;
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }

    }

    /**
     * Perform the TCP 3 way handshake. SYN -> SYNACK -> ACK
     * We poll and wait for a client to successfully connect,
     * when that client is connected we return the packet received
     * @return
     */
    private DatagramPacket handshake(){

        byte[] buffer = new byte[1024];
        DatagramPacket client = new DatagramPacket(buffer,buffer.length);

        try {
            serverSocket.receive(client);
            Segment s = Segment.deSerialize(client.getData());

            if(s.getMessageType() == MessageType.SYN){
                System.out.println("Received SYN request sending SYN ACK");
                byte[] data = Segment.serialize(new Segment(MessageType.SYN_ACK,0,s.getSequenceNumber() + 1,null));
                DatagramPacket packet = new DatagramPacket(data, data.length, client.getAddress(), client.getPort());
                serverSocket.send(packet);
                return client;
            }

        } catch (IOException e) {
            System.out.println("handshake with client failed");
            System.out.println(e.getMessage());
        }
        return null;
    }





    public static void main(String[] args) {
        new UDPThreadedServer(1080);
    }
}

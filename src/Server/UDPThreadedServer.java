package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #1
 * @student Id 7242530
 * @since Oct 6th , 2024
 */

public class UDPThreadedServer {

    private int gamePort;

    public UDPThreadedServer(int portNumber){

        gamePort = portNumber+1;

        try (
                DatagramSocket serverSocket = new DatagramSocket (portNumber);
        ) {
            while (true) {

                byte[] buffer1 = new byte[1024];
                byte[] buffer2 = new byte[1024];

                DatagramPacket client1 = new DatagramPacket(buffer1,buffer1.length);
                serverSocket.receive(client1);

                DatagramPacket client2 = new DatagramPacket(buffer2,buffer2.length);
                serverSocket.receive(client2);

                new UDPConnectionThread(gamePort,client1,client2).start();
                gamePort++;
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }

    }

    public static void main(String[] args) {
        new UDPThreadedServer(1080);
    }
}

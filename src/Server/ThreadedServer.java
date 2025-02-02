package Server;
import java.net.*;
import java.io.*;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #2
 * @student Id 7242530
 * @since Oct 25th , 2024
 */

public class ThreadedServer {

    public ThreadedServer(int portNumber){
        try (
                ServerSocket serverSocket = new ServerSocket(portNumber);
                //ServerSocket serverSocket = new ServerSocket(portNumber, 50, InetAddress.getByName("0.0.0.0"));

        ) {
            while (true) {
                Socket client1=serverSocket.accept();
                System.out.println("client 1 Connected");
                Socket client2=serverSocket.accept();
                System.out.println("client 2 Connected");
                new ConnectionThread(client1,client2).start();
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }

    }

    public static void main(String[] args) {
        new ThreadedServer(1080);
    }
}

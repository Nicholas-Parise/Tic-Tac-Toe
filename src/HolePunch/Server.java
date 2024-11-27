package HolePunch;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #3
 * @student Id 7242530
 * @since Nov 26th , 2024
 */

public class Server {
        // list of possible clients
        private static List<EndpointRegistry> servers = new ArrayList<>();
        private static int serverIndex;
        private static final int MAXCLIENTSPERSERVER = Integer.MAX_VALUE; // MAX CLIENTS PER SERVER this would be configured for how much a server can handle

    /**
     * simple loop that connects to clients and sends them the location of
     * the game server, This server is publicly addressable.
     * @param portNumber
     */
    public Server(int portNumber){
        serverIndex = 0;
            try (
                    ServerSocket serverSocket = new ServerSocket(portNumber);
            ) {
                while (true) {
                    Socket client1 = serverSocket.accept();
                    System.out.println("client Connected");
                    sendMessage(client1,servers.get(serverIndex).getHostName()+" "+servers.get(serverIndex).getPort());
                    servers.get(serverIndex).addConnectedClient();

                    if(servers.get(serverIndex).getConnectedClients()>=MAXCLIENTSPERSERVER){
                        serverIndex++;
                    }

                }
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
                System.out.println(e.getMessage());
            }
        }

    /**
     * simple boilerplate just send a string message to the socket (client)
     * @param socket
     * @param message
     */
    private static void sendMessage(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            System.err.println("Error sending message to client");
        }
    }

    public static void main(String[] args) {
        // add server endpoints IPs and PORTs here
        servers.add(new EndpointRegistry(1080,"localhost")); // server 1
        servers.add(new EndpointRegistry(1080,"localhost")); // server 2
        // in this example it's the same sever since there aren't any other known ones
        new Server(1079);
    }
}
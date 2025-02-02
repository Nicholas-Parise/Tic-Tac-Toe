package HolePunch;

import java.io.*;
import java.net.Socket;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #3
 * @student Id 7242530
 * @since Nov 26th , 2024
 */

public class Client {

    private static final int SERVERPORT = 1079;
    private static final String SERVERHOSTNAME = "localhost";

    /**
     * sends a GET to the server
     * it will then halt and wait to receive a response
     *
     * @return the IP and Port in the form 'IP PORT' both strings
     * @throws IOException
     */
    public static String getIPPort() throws IOException{

        Socket conn = new Socket(SERVERHOSTNAME, SERVERPORT);
        BufferedReader serverInput = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        sendMessage(conn,"GET");

        return serverInput.readLine();
    }

    /**
     * simple boilerplate function to make sending messages easier
     * @param socket the server socket
     * @param message message to send
     */
    private static void sendMessage(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            System.err.println("Error sending message to server");
        }
    }

}

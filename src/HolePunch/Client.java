package HolePunch;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

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

    public static String getIPPort(){
        try (
                Socket conn = new Socket(SERVERHOSTNAME, SERVERPORT);
                BufferedReader serverInput = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        ) {
            sendMessage(conn,"GET");

            String fromServer = serverInput.readLine();

            return fromServer;

        } catch (UnknownHostException e) {
            System.out.println("problem with the host name.");
        } catch (IOException e) {
            System.out.println("IO error for the connection.");
        }
        return "null null";
    }

    /**
     * just send a string message to the client
     * @param socket
     * @param message
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

/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
 * All Rights Reserved.
 **/

import java.io.*;
import java.net.*;

class SFTPServer {
    public static void main(String argv[]) throws Exception {
        // Binding to port 1155 which is technically not correct for SFTP but the application needs root privileges
        // to bind to port 115.
        int port = 1155;
        ServerSocket socket = new ServerSocket(port);
        System.out.println("Server listening on port: " + String.valueOf(port));

        // Listen for incoming connections to the server.
        //noinspection InfiniteLoopStatement
        while (true) {
            Socket s = socket.accept();
            Client client = new Client(s);
            client.start();
        }
    }
}

class Client extends Thread {
    private String user;
    private BufferedReader input;
    private DataOutputStream output;

    Client(Socket socket) throws IOException {
        System.out.println("Client connected on socket: " + String.valueOf(socket.getPort()));
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new DataOutputStream(socket.getOutputStream());
    }

    public void run() {
        // Continue to wait for commands until the NULL terminating character has been sent.
        //noinspection InfiniteLoopStatement
        while(true) {
            try {
                String clientSentence = this.readInput();
                String capitalizedSentence = clientSentence.toUpperCase() + '\n';
                this.writeOutput(capitalizedSentence);
            } catch (IOException e) {
                System.out.print("Error");
            }
        }
    }

    /**
     * Reads the input from the client until the null terminating character is sent.
     *
     * @return              the string the user has input
     * @throws IOException  an exception if error reading has occurred.
     */
    private String readInput() throws IOException {
        StringBuilder s = new StringBuilder();
        char c;

        do {
            c = (char) this.input.read();
            s.append(Character.toString(c));
        } while (c != '\0');

        return s.toString();
    }

    private void writeOutput(String message) throws IOException {
        output.writeBytes(message);
    }
}

//class SFTPException extends Throwable {
//    private String code;
//    private String message;
//}


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
    private Boolean bIsAuthenticated;
    private Socket socket;
    private BufferedReader input;
    private DataOutputStream output;

    Client(Socket socket) throws IOException {
        System.out.println("Client connected on socket: " + String.valueOf(socket.getPort()));
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new DataOutputStream(socket.getOutputStream());

        // Send first reply.
        try {
            this.writeOutput("+" + InetAddress.getLocalHost().getHostName() + " SFTP Service");
        } catch (IOException e) {
            try {
                this.writeOutput("-SFTP error, unable to obtain hostname");
                this.closeConnection();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void run() {
        String command;

        // Continue to wait for commands until the NULL terminating character has been sent.
        //noinspection InfiniteLoopStatement
        while(true) {
            try {
                command = this.readCommand();

                // Check to see if we have read in a command.
                if (command.length() > 0) {
                    // Deal with the current command being sent.

                    this.writeOutput("Hello");
                }

                // Check to see if the client is still connected.
                if (this.input.read() == -1) {
                    this.closeConnection();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() throws IOException {
        this.input.close();
        this.output.close();
        System.out.println("Client disconnected on socket: " + String.valueOf(this.socket.getPort()));
        this.socket.close();
    }

    /**
     * Reads the input from the client until the null terminating character is sent.
     * This method also makes the thread wait for input as long as the client is connected.
     *
     * @return              the string the user has input
     * @throws IOException  an exception if error reading has occurred.
     */
    private String readCommand() throws IOException {
        StringBuilder s = new StringBuilder();
        int c;

        do {
            c = this.input.read();
            s.append(String.valueOf((char)c));
        } while (c > 0);

        // Remove the null terminating character from the string.
        return s.toString().substring(0, s.toString().length() - 1);
    }

    private void writeOutput(String message) throws IOException {
        this.output.writeBytes(message + "\n");
    }
}

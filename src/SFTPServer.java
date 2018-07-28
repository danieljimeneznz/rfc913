/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
 * All Rights Reserved.
 **/

import java.io.*;
import java.net.*;
import java.util.List;

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

/**
 * Client class should technically be in its own file but leaving in the server file as this is the servers model
 * of a client.
 */
class Client extends Thread {
    String user;
    private boolean bIsAuthenticated;
    private Socket socket;
    private BufferedReader input;
    private DataOutputStream output;
    String currentDir;

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
        Command command;

        // Continue to wait for commands until the NULL terminating character has been sent.
        //noinspection InfiniteLoopStatement
        while(true) {
            try {
                int c = this.input.read();
                String s = this.readCommand(c);
                // Check to see if we have read in a command.
                if (s.length() > 0) {
                    // Deal with the current command being sent.
                    command = new Command(this, s);

                    switch (command.cmd) {
                        case "USER":
                            command.user();
                            break;
                        case "ACCT":
                            command.acct();
                            break;
                        case "PASS":
                            command.pass();
                            break;
                        case "TYPE":
                            command.type();
                            break;
                        case "LIST":
                            command.list();
                            break;
                        case "CDIR":
                            command.cdir();
                            break;
                        case "NAME":
                            command.name();
                            break;
                        case "DONE":
                            command.done();
                            return;
                        case "RETR":
                            command.retr();
                            break;
                        case "STOR":
                            command.stor();
                            break;
                    }
                }

                // Check to see if the client is still connected.
                if (c == -1) {
                    this.closeConnection();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void closeConnection() throws IOException {
        System.out.println("Client disconnected on socket: " + String.valueOf(this.socket.getPort()));
        this.socket.close();
        this.input.close();
        this.output.close();

    }

    /**
     * Reads the input from the client until the null terminating character is sent.
     * This method also makes the thread wait for input as long as the client is connected.
     *
     * @return              the string the user has input
     * @throws IOException  an exception if error reading has occurred.
     */
    private String readCommand(int i) throws IOException {
        StringBuilder s = new StringBuilder();
        int c = i;

        while (c > 0) {
            s.append(String.valueOf((char)c));
            c = input.read();
        }

        return s.toString();
    }

    void writeOutput(String message) throws IOException {
        this.output.writeBytes(message + '\0');
    }

    boolean isAuthenticated() {
        try {
            if (this.bIsAuthenticated) {
                return true;
            } else {
                // Inform the client that they are not authenticated.
                this.writeOutput("Hello");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

class Command {
    private Client client;
    String cmd;
    private String[] args;

    Command(Client client, String command) {
        this.client = client;

        this.cmd = command.substring(0, 4);
        if (command.length() > 4) {
            this.args = command.substring(5, command.length()).split(" ");
        }
    }

    void user() {
    }

    void acct() {
    }

    void pass() {
    }

    void type() {
        if (client.isAuthenticated()) {
            System.out.println("hello");
        }
    }

    void list() {
        if (client.isAuthenticated()) {
            System.out.println("hello");
        }
    }

    void cdir() {
        if (client.isAuthenticated()) {
            System.out.println("hello");

        }
    }

    void name() {
        if (client.isAuthenticated()) {
            System.out.println("hello");
        }
    }

    void done() {
        try {
            client.writeOutput("+" + InetAddress.getLocalHost().getHostName() + " Closing Connection");
            client.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void retr() {
        if (client.isAuthenticated()) {
            System.out.println("hello");
        }
    }

    void stor() {
        if (client.isAuthenticated()) {
            System.out.println("hello");
        }
    }
}
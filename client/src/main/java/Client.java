/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.io.*;
import java.net.*;

@SuppressWarnings("FieldCanBeLocal")
class Client {
    private boolean DEBUG = true;
    private BufferedReader userIn;
    private Socket socket;
    private DataOutputStream output;
    BufferedReader input;
    private String dir;
    private Command previousCommand;
    private int fileSize;
    private File file;

    /**
     * Create a new client object
     * @throws IOException  an error to do with the socket.
     */
    Client() throws IOException {
        this.userIn = new BufferedReader(new InputStreamReader(System.in));
        this.socket = new Socket("localhost", 1155);
        this.output = new DataOutputStream(socket.getOutputStream());
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.dir = System.getProperty("user.dir") + "/mnt/";
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String argv[]) throws Exception {
        // Create the new client instance that will connect to the server.
        Client client = new Client();
        try {
            // Read the first response from the server.
            System.out.println(Client.readInput(client.input));
            String command;

            // Listen for commands and send them to the server.
            while(true) {
                command = client.userIn.readLine();
                client.sendCommand(command);
                // When the user has finished, close the connection and exit gracefully.
                if (command.equals("DONE")) {
                    client.closeConnection();
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            client.closeConnection();
        }
    }

    /**
     * Reads the input from the client until the null terminating character is sent.
     * This method also makes the thread wait for input as long as the server is connected.
     *
     * @return the string the user has input
     * @throws IOException an exception if error reading has occurred.
     */
    static String readInput(BufferedReader input) throws IOException {
        StringBuilder s = new StringBuilder();
        int c = input.read();

        while (c > 0) {
            s.append(String.valueOf((char) c));
            c = input.read();
        }

        // Check to see if the server is still connected.
        if (c == -1) {
            throw new IOException();
        }

        // Remove the null terminating character from the string.
        return s.toString();
    }

    /**
     * This method sends the command to the server. It also listens to responses from the server and handles
     * file uploads/downloads.
     * @param cmd   the command to be sent to the server
     * @throws IOException an exception if and error has occurred.
     */
    @SuppressWarnings("ConstantConditions")
    void sendCommand(String cmd) throws IOException {
        Command command = new Command(cmd);
        File file = null;
        if (DEBUG) System.out.println("Sending command: " + cmd);

        // Check that the file to be sent actually exists.
        if (command.cmd.equals("STOR")) {
            file = new File(this.dir + command.args[1]);
            if (!file.exists()) {
                System.out.println("-File does not exist on client");
                return;
            }
        }

        // Check that the file to be sent actually exists.
        if (command.cmd.equals("SIZE")) {
            if (this.previousCommand.cmd.equals("STOR")) {
                file = new File(this.dir + this.previousCommand.args[1]);
                if (!file.exists()) {
                    System.out.println("-File does not exist on client");
                    return;
                }
            } else {
                System.out.println("-Couldn't save because previous command was not STOR or STOR command failed");
                return;
            }
        }

        // SEND THE COMMAND.
        this.output.writeBytes(cmd + "\0");

        // Receive the file being sent from the server.
        if (command.cmd.equals("SEND") && !this.previousCommand.cmd.equals("STOP")) {
            file = new File(this.dir + this.previousCommand.args[0]);
            FileOutputStream out = new FileOutputStream(file);

            while (file.length() < this.fileSize) {
                out.write(this.input.read());
            }
            out.close();
            return;
        }

        // Wait for a response from the server.
        String s = Client.readInput(this.input);
        if (s.length() > 0) {
            System.out.println(s);
        }

        // Check to see that the RETR command succeeded and if it does, get the file size to be sent from the server.
        if (command.cmd.equals("RETR")) {
            if (s.charAt(0) != '-') {
                this.fileSize = Integer.valueOf(s);
                file = new File(this.dir + command.args[0]);

                // Automatically send stop command if file already exists on client.
                if (file.exists()) {
                    this.sendCommand("STOP");
                    return;
                }

                // Automatically send stop command if there is not enough space available on client.
                if ((new File(dir)).getUsableSpace() < this.fileSize) {
                    this.sendCommand("STOP");
                    return;
                }
            }
        }

        // Check to see if STOR command succeeded and set the file.
        if (command.cmd.equals("STOR")) {
            if (s.charAt(0) != '-') {
                this.file = file;
            }
        }

        // Start sending the file to the server if the client specified the SIZE command.
        if (command.cmd.equals("SIZE")) {
            if (s.charAt(0) != '-') {
                // Send file to server.
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(this.file));

                int data;
                // Read and send file until the whole file has been sent
                while ((data = bufferedInputStream.read()) != -1) {
                    this.output.write(data);
                }
                bufferedInputStream.close();
                this.output.flush();

                s = Client.readInput(this.input);
                if (s.length() > 0) {
                    System.out.println(s);
                }
            }
        }

        this.previousCommand = command;
    }

    /**
     * Close the connection to the server.
     * @throws IOException  an exception if an error occurred.
     */
    void closeConnection() throws IOException {
        userIn.close();
        output.close();
        input.close();
        System.out.println("Server disconnected on socket: " + String.valueOf(socket.getPort()));
        System.out.println("Closed connection");
        socket.close();
    }
}

/**
 * Command class is an abstraction of the command that is to be sent.
 */
class Command {
    String cmd;
    String[] args;

    /**
     * Create a new command that contains information regarding what the user has typed.
     * @param command   the command that was sent (as a string).
     */
    Command(String command) {
        try {
            this.cmd = command.substring(0, 4);
        } catch (StringIndexOutOfBoundsException e) {
            this.cmd = "";
            return;
        }
        if (command.length() > 4) {
            this.args = command.substring(5, command.length()).split(" ");
        }
    }
}
/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.io.*;
import java.net.*;

class Client {
    private BufferedReader userIn;
    private Socket socket;
    private DataOutputStream output;
    private BufferedReader input;
    private String dir;
    private Command previousCommand;
    private int filesize;

    private Client() throws IOException {
        this.userIn = new BufferedReader(new InputStreamReader(System.in));
        this.socket = new Socket("localhost", 1155);
        this.output = new DataOutputStream(socket.getOutputStream());
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.dir = System.getProperty("user.dir") + "/mnt/";

    }

    public static void main(String argv[]) throws Exception {
        Client client = new Client();
        try {
            // TODO: WILL NEED COMMANDS TO BE EXECUTED AFTER RETRIEVING THE RESPONSE FROM THE SERVER. I.E.
            // TODO: SEND COMMAND, WAIT RESPONSE, DO AGAIN. (ON FIRST RUN RECEIVE RESPONSE THEN SEND COMMAND).

            System.out.println(Client.readInput(client.input));
//          client.sendCommand("USER 1");
//          client.sendCommand("USER 3");
//          client.sendCommand("USER 6");
            client.sendCommand("TYPE a");
            client.sendCommand("PASS test");
            client.sendCommand("ACCT admin");
            client.sendCommand("USER 1");
            client.sendCommand("ACCT test");
            client.sendCommand("LIST F");
            client.sendCommand("LIST F hello");
            client.sendCommand("LIST F empty");
            client.sendCommand("LIST V");
            client.sendCommand("CDIR test");
            client.sendCommand("CDIR empty");
            client.sendCommand("PASS dafafa");
            client.sendCommand("CDIR /");
            client.sendCommand("PASS test");
            client.sendCommand("CDIR /");
            client.sendCommand("KILL kill.txt");
            client.sendCommand("NAME a.txt");
            client.sendCommand("TOBE c.txt");
//          client.sendCommand("NAME c.txt");
//          client.sendCommand("TOBE a.txt");
            client.sendCommand("NAME abc.txt");
            client.sendCommand("LIST V");
            client.sendCommand("RETR b.txt");
            client.sendCommand("SEND");
            client.sendCommand("RETR c.txt");
            client.sendCommand("LIST V");
            client.sendCommand("DONE");
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
    private static String readInput(BufferedReader input) throws IOException {
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

    private void sendCommand(String cmd) throws IOException {
        Command command = new Command(cmd);
        System.out.println("Sending command: " + cmd);
        this.output.writeBytes(cmd + "\0");

        if (command.cmd.equals("SEND") && !this.previousCommand.cmd.equals("STOP")) {
            File file = new File(this.dir + this.previousCommand.args[0]);
            FileOutputStream out = new FileOutputStream(file);

            while (file.length() < this.filesize) {
                out.write(this.input.read());
            }
            out.close();
            return;
        }

        String s = Client.readInput(this.input);
        if (s.length() > 0) {
            System.out.println(s);
        }

        if (command.cmd.equals("RETR")) {
            if (s.charAt(0) != '-') {
                this.filesize = Integer.valueOf(s);
                File file = new File(this.dir + command.args[0]);

                // Automatically send stop command if file already exists on client.
                if (file.exists()) {
                    this.sendCommand("STOP");
                    return;
                }

                if ((new File(dir)).getUsableSpace() < this.filesize) {
                    this.sendCommand("STOP");
                    return;
                }
            }
        }
        this.previousCommand = command;
    }

    private void closeConnection() throws IOException {
        userIn.close();
        output.close();
        input.close();
        System.out.println("Server disconnected on socket: " + String.valueOf(socket.getPort()));
        System.out.println("Closed connection");
        socket.close();
    }
} 


class Command {
    String cmd;
    String[] args;

    Command(String command) {
        this.cmd = command.substring(0, 4);
        if (command.length() > 4) {
            this.args = command.substring(5, command.length()).split(" ");
        }
    }
}
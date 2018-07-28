import java.io.*;
import java.net.*;

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
import java.io.*;
import java.net.*;

class Client extends Thread {
    User user;
    boolean bIsAuthenticated;
    private Socket socket;
    private BufferedReader input;
    private DataOutputStream output;
    private String currentDir;

    Client(Socket socket) throws IOException {
        System.out.println("Client connected on socket: " + String.valueOf(socket.getPort()));
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new DataOutputStream(socket.getOutputStream());
        this.currentDir = System.getProperty("user.dir" + "/mnt");
        this.user = new User();

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

    @SuppressWarnings("ConstantConditions")
    boolean isAuthenticated() {
        if (this.bIsAuthenticated) {
            return true;
        } else {
            // Test to see if the user is authenticated by checking each of the user values specified against those
            // in the users file.
            Users users = Server.getUsers();
            User u = users.getUser(this.user.id, this.user.acct, this.user.pass);

            if (u == null) {
                return false;
            }
            this.bIsAuthenticated = true;
            return true;
        }
    }
}
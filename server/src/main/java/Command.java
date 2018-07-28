import java.io.*;
import java.net.*;

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

//    void readUserFile() {
//        JSONObject obj = new JSONObject();
//    }

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


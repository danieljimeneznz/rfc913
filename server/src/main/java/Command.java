import java.io.IOException;
import java.net.InetAddress;

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

    @SuppressWarnings("ConstantConditions")
    void user() {
        try {
            // First check that the arg was given.
            if (args.length > 1) {
                this.client.writeOutput("-Invalid user-id, try again");
                return;
            }

            // Try to find the current user based on id.
            Users users = Server.getUsers();
            User u = users.getUser(args[0]);
            if (u == null) {
                // User does not exist.
                System.out.println(args[0] + ": invalid user-id attempted to log in to server");
                this.client.writeOutput("-Invalid user-id, try again");
                return;
            }

            if (u.acct.equals("") || this.client.isAuthenticated()) {
                this.client.bIsAuthenticated = true;
                this.client.user = u;
                System.out.println(u.id + ": authenticated with server");
                this.client.writeOutput("!" + u.id + " logged in");
                return;
            }

            this.client.user.id = u.id;
            System.out.println(u.id + ": logged in to server");
            this.client.writeOutput("+User-id valid, send account and password");
        } catch (IOException e) {
            e.printStackTrace();
        }
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


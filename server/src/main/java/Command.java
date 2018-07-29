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
            User u = users.getUserByID(args[0]);
            if (u == null) {
                // User does not exist.
                System.out.println(args[0] + ": invalid user-id attempted to log in to server");
                this.client.writeOutput("-Invalid user-id, try again");
                return;
            }

            this.client.user.id = u.id;
            if (u.acct.equals("") || this.client.isAuthenticated()) {
                this.client.user = u;
                System.out.println(u.id + ": authenticated with server");
                this.client.writeOutput("!" + u.id + " logged in");
                return;
            }

            System.out.println(u.id + ": sent user id to server");
            this.client.writeOutput("+User-id valid, send account and password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ConstantConditions")
    void acct() {
        try {
            // First check that the arg was given.
            if (args.length > 1) {
                this.client.writeOutput("-Invalid account, try again");
                return;
            }

            Users users = Server.getUsers();
            User u = users.getUserByAcct(args[0]);
            if (u == null) {
                // User does not exist.
                System.out.println(args[0] + ": invalid account attempted to log in to server");
                this.client.writeOutput("-Invalid account, try again");
                return;
            }

            this.client.user.acct = u.acct;
            if (this.client.isAuthenticated()) {
                this.client.user = u;
                System.out.println(u.id + ": authenticated with server");
                this.client.writeOutput("!Account valid, logged-in");
                return;
            }

            System.out.println(u.id + ": sent account to server");
            this.client.writeOutput("+Account valid, send password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ConstantConditions")
    void pass() {
        try {
            // First check that the arg was given.
            if (args.length > 1) {
                this.client.writeOutput("-Wrong password, try again");
                return;
            }

            this.client.user.pass = args[0];
            if (this.client.isAuthenticated()) {
                System.out.println(this.client.user.id + ": authenticated with server");
                this.client.writeOutput("!Logged in");
                return;
            }

            // Check password against those in list if client hasn't specified an account.
            Users users = Server.getUsers();
            if (users.checkPass(args[0])) {
                System.out.println("User password correct but no user specified");
                this.client.writeOutput("+Send account");
                return;
            }

            this.client.writeOutput("-Wrong password, try again");
        } catch (IOException e) {
            e.printStackTrace();
        }
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


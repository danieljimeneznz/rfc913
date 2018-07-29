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
            // First check that an arg was given.
            if (checkArguments(1)) {
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
            if (u.pass.equals("") || this.client.checkAuthentication()) {
                this.client.user = u;
                System.out.println(u.id + ": authenticated with server");
                this.client.writeOutput("!" + u.id + " logged in");
                return;
            }

            System.out.println(u.id + ": sent user id to server");
            if (this.client.user.acct != null) {
                this.client.writeOutput("+User-id valid, send password");
            } else {
                this.client.writeOutput("+User-id valid, send account and password");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ConstantConditions")
    void acct() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
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
            if (this.client.checkAuthentication()) {
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
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            this.client.user.pass = args[0];
            if (this.client.checkAuthentication()) {
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
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            if (client.isAuthenticated()) {
                switch (args[0]) {
                    case "A":
                        this.client.transmissionType = "A";
                        this.client.writeOutput("+Using Ascii mode");
                        break;
                    case "B":
                        this.client.transmissionType = "B";
                        this.client.writeOutput("+Using Binary mode");
                        break;
                    case "C":
                        this.client.transmissionType = "C";
                        this.client.writeOutput("+Using Continuous mode");
                        break;
                    default:
                        this.client.writeOutput("-Type not valid");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    private boolean checkArguments(int amount) throws IOException {
        // First check that the arg was given.
        if (args.length != amount) {
            this.client.writeOutput("-Invalid amount of arguments given");
            return true;
        }
        return false;
    }
}


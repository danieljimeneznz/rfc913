import org.apache.commons.io.comparator.NameFileComparator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.text.SimpleDateFormat;
import java.util.Arrays;

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
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }
            if (client.isAuthenticated()) {
                // Get the directory from the argument or set to the current dir if not specified.
                String directory = args.length == 2 ? args[1] : null;
                if (directory == null) {
                    directory = this.client.currentDir;
                } else {
                    // Make sure directory is within the mount dir if it is absolute. Otherwise append it to the currentdir.
                    if (directory.charAt(0) == '/') {
                        directory = this.client.mountDir + directory;
                    } else {
                        directory = this.client.currentDir + "/" + directory;
                    }
                }

                if (!args[0].equals("F") && !args[0].equals("V")) {
                    this.client.writeOutput("-Directory listing format not valid");
                    return;
                }

                File folder = new File(directory);
                File[] files = folder.listFiles();
                if (!folder.exists() || !folder.isDirectory()) {
                    this.client.writeOutput("-Directory does not exist");
                    return;
                }

                StringBuilder response = new StringBuilder();
                response.append("+").append(directory);
                if (files != null && files.length > 0) {
                    response.append("\n");
                    Arrays.sort(files, NameFileComparator.NAME_COMPARATOR);
                    // We now have the correct directory path to perform the LIST command on.
                    switch (args[0]) {
                        case "F":
                            for (int i = 0; i < files.length; i++) {
                                response.append(files[i].getName());
                                if (i != files.length - 1) {
                                    response.append("\n");
                                }
                            }
                            break;
                        case "V":
                            // Verbose output = filename    size    modified time   owner
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                            for (int i = 0; i < files.length; i++) {
                                Path path = Paths.get(files[i].getAbsolutePath());
                                FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
                                response.append(files[i].getName()).append("\t").append(files[i].length()).append("\t").append(sdf.format(files[i].lastModified())).append("\t").append(ownerAttributeView.getOwner());
                                if (i != files.length - 1) {
                                    response.append("\n");
                                }
                            }
                            break;
                    }
                }
                this.client.writeOutput(response.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void cdir() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            // Get the directory from the argument or set to the current dir if not specified.
            String directory = args[0];

            // Make sure directory is within the mount dir if it is absolute. Otherwise append it to the currentdir.
            if (directory.charAt(0) == '/') {
                directory = this.client.mountDir + directory;
            } else {
                directory = this.client.currentDir + "/" + directory;
            }

            File folder = new File(directory);
            if (folder.exists() && folder.isDirectory()) {
                if (!client.checkAuthentication()) {
                    this.client.writeOutput("+Directory ok, send account/password");
                } else {
                    this.client.currentDir = directory;
                    this.client.writeOutput("!Changed working dir to " + args[0]);
                }
            } else {
                this.client.writeOutput("-Can't connect to directory because it does not exist");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void kill() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            if (client.isAuthenticated()) {
                File file = new File(this.client.currentDir + args[0]);

                if (file.exists())
                {
                    boolean result = file.delete();
                    if (result) {
                        this.client.writeOutput("+" + args[0] + " deleted");
                    } else {
                        this.client.writeOutput("-Failed to delete file or folder");
                    }
                    return;
                }
                this.client.writeOutput("-Not deleted because file or folder does not exist");
                return;
            }
            this.client.writeOutput("-Not deleted because user not authenticated");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void name() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            if (client.isAuthenticated()) {
                File file = new File(this.client.currentDir + args[0]);
                if (!file.exists()) {
                    this.client.writeOutput("-Can't find " + args[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void tobe() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            if (client.isAuthenticated()) {
                if (!this.client.previousCommand.cmd.equals("NAME")) {
                    this.client.writeOutput("-File wasn't renamed because client did not send NAME command before");
                    return;
                }

                File file = new File(this.client.currentDir + this.client.previousCommand.args[0]);
                if (!file.exists()) {
                    this.client.writeOutput("-Can't find " + this.client.previousCommand.args[0]);
                    return;
                }

                // Rename file.
                boolean result = file.renameTo(new File(this.client.currentDir + args[0]));
                if (result) {
                    this.client.writeOutput("+" + this.client.previousCommand.args[0] + " renamed to " + args[0]);
                } else {
                    this.client.writeOutput("-Failed to rename file");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            if (client.isAuthenticated()) {
                File file = new File(this.client.currentDir + args[0]);

                if (!file.exists()) {
                    this.client.writeOutput("-File doesn't exist");
                } else {
                    this.client.writeOutput(String.valueOf(file.length()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void send() {
        try {
            if (client.isAuthenticated()) {
                if (this.client.previousCommand.cmd.equals("RETR")) {
                    File file = new File(this.client.currentDir + this.client.previousCommand.args[0]);

                    if (!file.exists()) {
                        this.client.writeOutput("-File doesn't exist");
                    } else {
                        // Send file to client.
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));

                        int data;
                        // Read and send file until the whole file has been sent
                        while ((data = bufferedInputStream.read()) != -1) {
                            this.client.output.write(data);
                        }
                        bufferedInputStream.close();
                        this.client.output.flush();
                    }
                } else {
                    this.client.writeOutput("-Client did not send RETR command before");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stop() {
        try {
            if (client.isAuthenticated()) {
                if (this.client.previousCommand.cmd.equals("RETR")) {
                    this.client.writeOutput("+ok, RETR aborted");
                } else {
                    this.client.writeOutput("-Client did not send RETR command before");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stor() {
        try {
            // First check that an arg was given.
            if (checkArguments(2)) {
                return;
            }

            if (client.isAuthenticated()) {
                System.out.println("hsello");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void size() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            if (client.isAuthenticated()) {
                System.out.println("hello");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkArguments(int amount) throws IOException {
        // First check that the arg was given.
        if (args.length < amount) {
            this.client.writeOutput("-Invalid amount of arguments given");
            return true;
        }
        return false;
    }
}


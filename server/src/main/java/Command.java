import org.apache.commons.io.comparator.NameFileComparator;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.text.SimpleDateFormat;
import java.util.Arrays;

class Command {
    private boolean DEBUG = false;
    private Client client;
    String cmd;
    private String[] args;
    private File file;

    /**
     * Create a new command that contains information regarding what has been sent by the client.
     * @param client    the client object on the server that sent the command.
     * @param command   the command that was sent (as a string).
     */
    Command(Client client, String command) {
        this.client = client;

        // Grab the command and arguments from the command.
        try {
            this.cmd = command.substring(0, 4);
        } catch (StringIndexOutOfBoundsException e) {
            try {
                this.cmd = "";
                this.client.writeOutput("-Invalid Command length");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
        if (command.length() > 4) {
            this.args = command.substring(5, command.length()).split(" ");
        }
        this.file = null;
    }

    /**
     * User command sets the user id for the client object and checks to see if enough information is valid
     * to log the user into the server.
     */
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
                if (DEBUG) System.out.println(args[0] + ": invalid user-id attempted to log in to server");
                this.client.writeOutput("-Invalid user-id, try again");
                return;
            }

            this.client.user.id = u.id;
            if (u.pass.equals("") || this.client.checkAuthentication()) {
                this.client.user = u;
                if (DEBUG) System.out.println(u.id + ": authenticated with server");
                this.client.writeOutput("!" + u.id + " logged in");
                return;
            }

            // User id sent but account or password missing.
            if (DEBUG) System.out.println(u.id + ": sent user id to server");
            if (this.client.user.acct != null) {
                this.client.writeOutput("+User-id valid, send password");
            } else {
                this.client.writeOutput("+User-id valid, send account and password");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Acct command sets the acct for the client object and checks to see if enough information is valid
     * to log the user into the server.
     */
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
                if (DEBUG) System.out.println(args[0] + ": invalid account attempted to log in to server");
                this.client.writeOutput("-Invalid account, try again");
                return;
            }

            this.client.user.acct = u.acct;
            if (this.client.checkAuthentication()) {
                this.client.user = u;
                if (DEBUG) System.out.println(u.id + ": authenticated with server");
                this.client.writeOutput("!Account valid, logged-in");
                return;
            }

            if (DEBUG) System.out.println(u.id + ": sent account to server");
            this.client.writeOutput("+Account valid, send password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pass command sets the password for the client object and checks to see if enough information is valid
     * to log the user into the server.
     */
    @SuppressWarnings("ConstantConditions")
    void pass() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            this.client.user.pass = args[0];
            if (this.client.checkAuthentication()) {
                if (DEBUG) System.out.println(this.client.user.id + ": authenticated with server");
                this.client.writeOutput("!Logged in");
                return;
            }

            // Check password against those in list if client hasn't specified an account.
            Users users = Server.getUsers();
            if (users.checkPass(args[0])) {
                if (DEBUG) System.out.println("User password correct but no user specified");
                this.client.writeOutput("+Send account");
                return;
            }

            this.client.writeOutput("-Wrong password, try again");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Type command sets the client transmission type to the respective types.
     */
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

    /**
     * List command lists the directory listing either as a list of files or a verbose list of files in the
     * directory.
     */
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

                // Check to see if the folder exists and grab the files from the folder.
                File folder = new File(directory);
                File[] files = folder.listFiles();
                if (!folder.exists() || !folder.isDirectory()) {
                    this.client.writeOutput("-Directory does not exist");
                    return;
                }

                // Start building the response to the client.
                StringBuilder response = new StringBuilder();
                response.append("+").append(directory);
                if (files != null && files.length > 0) {
                    response.append("\n");
                    // Sort the files by alphabetical filename.
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

    /**
     * Cdir command attempts to cd into the given relative or absolute directory (absolute directory is actually
     * relative to the mount directory specified by the server).
     */
    void cdir() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            // Get the directory from the argument or set to the current dir if not specified.
            String directory = args[0];

            // Make sure directory is within the mount dir if it is absolute. Otherwise append it to the current dir.
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

    /**
     * Kill command deletes a file.
     */
    void kill() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            if (client.isAuthenticated()) {
                File file = new File(this.client.currentDir + args[0]);

                // Check the file exists and attempt to delete it.
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

    /**
     * Name command sets up a file rename by checking that the file to be renamed actually exists.
     */
    void name() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            if (client.isAuthenticated()) {
                // Get file name from command and check it is valid.
                File file = new File(this.client.currentDir + args[0]);
                if (!file.exists()) {
                    this.client.writeOutput("-Can't find " + args[0]);
                } else {
                    this.client.writeOutput("+File exists");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tobe command informs the server as to what the previously selected file should be renamed to.
     */
    void tobe() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            if (client.isAuthenticated()) {
                // Check that the NAME command was previously given.
                if (!this.client.previousCommand.cmd.equals("NAME")) {
                    this.client.writeOutput("-File wasn't renamed because client did not send NAME command before");
                    return;
                }

                // Check that the file still exists.
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

    /**
     * Done command disconnects the server from the client.
     */
    void done() {
        try {
            client.writeOutput("+" + InetAddress.getLocalHost().getHostName() + " Closing Connection");
            client.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retr command informs the server that the client would like to get a file from the server and informs the
     * client how many bytes it will send.
     */
    void retr() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            if (client.isAuthenticated()) {
                File file = new File(this.client.currentDir + args[0]);

                // Check file exists and send the file size to the client.
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

    /**
     * Send command sends the file to the client.
     */
    void send() {
        try {
            if (client.isAuthenticated()) {
                if (this.client.previousCommand.cmd.equals("RETR")) {
                    File file = new File(this.client.currentDir + this.client.previousCommand.args[0]);

                    // Check that the file still exists.
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

    /**
     * Stop command stops the retrieval command.
     */
    void stop() {
        try {
            if (client.isAuthenticated()) {
                // Check that the previous command was RETR.
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

    /**
     * Stor command informs the server that the client would like to send a file. It also specifies how the server
     * should recieve the file.
     */
    void stor() {
        try {
            // First check that an arg was given.
            if (checkArguments(2)) {
                return;
            }

            if (client.isAuthenticated()) {
                File file = new File(this.client.currentDir + args[1]);
                switch (args[0]) {
                   case "NEW":
                       if (file.exists()) {
                           this.client.writeOutput("+File exists, will create new generation of file");
                           String fileName = this.client.currentDir + args[1];
                           int i = 1;
                           // Find a new generation of file that is currently not taken, if that number is greater than
                           // 1000 then don't bother attempting to create a new generation.
                           while (file.exists()) {
                               String newFileName = fileName.substring(0, fileName.lastIndexOf('.')) + "-" + String.valueOf(i) + fileName.substring(fileName.lastIndexOf('.'));
                               file = new File(newFileName);
                               i++;

                               if (i > 1000) {
                                   this.client.writeOutput("-File exists, but system doesn't support generations");
                                   return;
                               }
                           }
                       } else {
                           this.client.writeOutput("+File does not exist, will create new file");
                       }
                       break;
                   case "OLD":
                       if (file.exists()) {
                           this.client.writeOutput("+Will write over old file");
                       } else {
                           this.client.writeOutput("+Will create new file");
                       }
                       break;
                   case "APP":
                       if (file.exists()) {
                           this.client.writeOutput("+Will append to file");
                       } else  {
                           this.client.writeOutput("+Will create file");
                       }
                       break;
                   default:
                       this.client.writeOutput("-Storage mode not valid");
                       return;
                }
                // Store the filename in the current command.
                this.file = file;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Size command informs the server the size of the file to be sent by the client who then proceeds to send the
     * file.
     */
    @SuppressWarnings("IfCanBeSwitch")
    void size() {
        try {
            // First check that an arg was given.
            if (checkArguments(1)) {
                return;
            }

            if (client.isAuthenticated()) {
                // Check that the previous command saved the filename.
                if (this.client.previousCommand.file == null) {
                    this.client.writeOutput("-Couldn't save because previous command was not STOR or STOR command failed");
                    return;
                }

                // Get the size of the file from the argument.
                int fileSize = Integer.valueOf(args[0]);
                if (this.client.previousCommand.args[0].equals("APP")) {
                    fileSize = (int) (this.client.previousCommand.file.length() + fileSize);
                }

                // Ensure there is enough room on the server to store the file.
                if ((new File(this.client.currentDir)).getUsableSpace() < fileSize) {
                    this.client.writeOutput("-Not enough room, don't send it");
                    return;
                } else {
                    this.client.writeOutput("+ok, waiting for file");
                }

                // Write to the file, appending, or overwriting the file as per the instructions of the previous command.
                File file = this.client.previousCommand.file;
                FileOutputStream out;
                if (this.client.previousCommand.args[0].equals("NEW") || this.client.previousCommand.args[0].equals("OLD")) {
                    out = new FileOutputStream(file);
                } else if (this.client.previousCommand.args[0].equals("APP")) {
                    out = new FileOutputStream(file, true);
                } else {
                    this.client.writeOutput("-Storage mode not valid");
                    return;
                }

                while (file.length() < fileSize) {
                    out.write(this.client.input.read());
                }
                out.close();
                this.client.writeOutput("+Saved " + file.getName());
            }
        } catch (NumberFormatException e) {
            try {
                this.client.writeOutput("-File size not valid integer");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks the amount of arguments given to a particular command is above the amount required by the command.
     * @param amount    amount of arguments that should be specified.
     * @return          a boolean indicating whether the amount of required arguments was given.
     * @throws IOException  exception when client socket is closed.
     */
    private boolean checkArguments(int amount) throws IOException {
        // First check that the arg was given.
        if (args == null || args.length < amount) {
            this.client.writeOutput("-Invalid amount of arguments given");
            return true;
        }
        return false;
    }
}


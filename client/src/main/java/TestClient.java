import java.io.IOException;

/**
 * Class used for testing the server by simulating commands written by a client.
 */
public class TestClient {
    public static void main(String argv[]) throws Exception {
        Client client = new Client();
        try {
            System.out.println(Client.readInput(client.input));               // Server Reply: '<HOSTNAME> SFTP Service'
            TestClient.sendCommand(client, "TYPE a");                // Server Reply: '-User not authenticated' as no user has been sent yet.
            TestClient.sendCommand(client, "PASS test");             // Server Reply: '+Send account' as password has been sent but not user or account.
            TestClient.sendCommand(client, "ACCT admin");            // Server Reply: '+Account valid, send password' as account valid but previous password wrong for account.
            TestClient.sendCommand(client, "USER 1");                // Server Reply: '+User-id valid, send password' as user id valid but password wrong.
            TestClient.sendCommand(client, "ACCT test");             // Server Reply: '!Account valid, logged-in' as acct, user and pass all correct.
            TestClient.sendCommand(client, "LIST F");                // Server Reply: '+/Users/dan/IdeaProjects/rfc913/server/mnt    a.txt   b.txt   empty   kill.txt    test.txt' which are the files in the mnt directory.
            TestClient.sendCommand(client, "LIST F hello");          // Server Reply: '-Directory does not exist' as hello directory not in server mnt.
            TestClient.sendCommand(client, "LIST F empty");          // Server Reply: '+/Users/dan/IdeaProjects/rfc913/server/mnt/empty' as directory exists but is empty.
            TestClient.sendCommand(client, "LIST V");                // Server Reply: '+/Users/dan/IdeaProjects/rfc913/server/mnt   a.txt	1	08/02/2018 19:36:18	<user>  b.txt	7	08/03/2018 14:47:53	<user>  empty	64	07/30/2018 15:00:47	<user>  kill.txt	4	08/05/2018 17:59:21	<user>  test.txt	34	08/05/2018 17:58:53	<user>'
            TestClient.sendCommand(client, "CDIR test");             // Server Reply: '-Can't connect to directory because it does not exist' as test dir not in mnt.
            TestClient.sendCommand(client, "CDIR empty");            // Server Reply: '!Changed working dir to empty'
            TestClient.sendCommand(client, "PASS dafafa");           // Server Reply: '-Wrong password, try again' user will now be signed out as they have provided an invalid password.
            TestClient.sendCommand(client, "CDIR /");                // Server Reply: '+Directory ok, send account/password' as user logged out but directory exists.
            TestClient.sendCommand(client, "PASS test");             // Server Reply: '!Logged in' as user has re-logged in.
            TestClient.sendCommand(client, "CDIR /");                // Server Reply: '!Changed working dir to /' to go back to root mnt directory.
            TestClient.sendCommand(client, "KILL kill.txt");         // Server Reply: '+kill.txt deleted', this will delete the kill file.
            TestClient.sendCommand(client, "NAME a.txt");            // Server Reply: '+File exists' as the file exists and can be renamed.
            TestClient.sendCommand(client, "TOBE c.txt");            // Server Reply: '+a.txt renamed to c.txt', c.txt should now be seen in server mnt folder.
            TestClient.sendCommand(client, "NAME abc.txt");          // Server Reply: '-Can't find abc.txt' since file does not exist on server.
            TestClient.sendCommand(client, "LIST V");                // Server Reply: '-File wasn't renamed because command was not TOBE' as the command after 'NAME' should be 'TOBE".
            TestClient.sendCommand(client, "RETR b.txt");            // Server Reply: '7' as that is the file size.
            TestClient.sendCommand(client, "SEND");                  // Server Reply: '' server does not reply as the file is being downloaded to the client.
            TestClient.sendCommand(client, "RETR c.txt");            // Server Reply: '1' as that is the file size.
            TestClient.sendCommand(client, "LIST V");                // Server Reply: '-File wasn't sent because command was not SEND or STOP', as server was expecting those two commands after the 'RETR' command.
            TestClient.sendCommand(client, "STOR NEW blah.txt");     // Server Reply: '-File does not exist on client' as the client is attempting to upload a file that does not exist.
            TestClient.sendCommand(client, "SIZE 28");               // Server Reply: '-Couldn't save because previous command was not STOR or STOR command failed', as server expects the 'STOR' command before 'SIZE'.
            TestClient.sendCommand(client, "SIZE 68");               // Server Reply: '-Couldn't save because previous command was not STOR or STOR command failed', as server expects the 'STOR' command before 'SIZE'.
            TestClient.sendCommand(client, "STOR OLD test.txt");     // Server Reply: '+Will create new file' as test.txt does not currently exist on the server.
            TestClient.sendCommand(client, "SIZE 17");               // Server Reply: '+ok, waiting for file     +Saved test.txt', the test.txt is successfully uploaded to the server.
            TestClient.sendCommand(client, "STOR APP test.txt");     // Server Reply: '+Will append to file' as test.txt exists and append has been specified by the user.
            TestClient.sendCommand(client, "SIZE 17");               // Server Reply: '+ok, waiting for file     +Saved test.txt', the test.txt will have the contents appended to the file (without a newline when the append occurs).
            TestClient.sendCommand(client, "STOR NEW test.txt");     // Server Reply: '+File exists, will create new generation of file', server will create a new generation.
            TestClient.sendCommand(client, "SIZE 17");               // Server Reply: '+ok, waiting for file     +Saved test-1.txt', the test.txt is successfully uploaded to the server but saved as test-1.txt.
            TestClient.sendCommand(client, "RETR test-1.txt");       // Server Reply: '17', as this is the size of test-1.txt on the server mnt folder.
            TestClient.sendCommand(client, "SEND");                  // Server Reply: '' server does not reply as the file is being downloaded to the client.
            TestClient.sendCommand(client, "STOR APP test-1.txt");   // Server Reply: '+Will append to file', server will append the contents to the test-1.txt file.
            TestClient.sendCommand(client, "SIZE 17");               // Server Reply: '+ok, waiting for file     +Saved test-1.txt'. After this operation, the server will have test.txt and test-1.txt which will be the same but the client test-1.txt will be the same as the clients test.txt.
            TestClient.sendCommand(client, "STOR OLD test-1.txt");   // Server Reply: '+Will write over old file', server will replace the test-1.txt with client copy.
            TestClient.sendCommand(client, "SIZE 17");               // Server Reply: '+ok, waiting for file     +Saved test-1.txt', server and client copy of this file will be the same.
            TestClient.sendCommand(client, "DONE");                  // Server Reply: '<HOSTNAME> Closing Connection'
        } catch (IOException e) {
            client.closeConnection();
        }
    }

    /**
     * Helper method that writes the command to console before sending the command to the server.
     */
    private static void sendCommand(Client client, String command) throws IOException {
        System.out.println("Sending command:" + command);
        client.sendCommand(command);
    }
}
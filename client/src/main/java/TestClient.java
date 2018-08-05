import java.io.IOException;

public class TestClient {
    public static void main(String argv[]) throws Exception {
        Client client = new Client();
        try {
            System.out.println(Client.readInput(client.input));
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
            client.sendCommand("NAME abc.txt");
            client.sendCommand("LIST V");
            client.sendCommand("RETR b.txt");
            client.sendCommand("SEND");
            client.sendCommand("RETR c.txt");
            client.sendCommand("LIST V");
            client.sendCommand("STOR NEW blah.txt");
            client.sendCommand("SIZE 28");
            client.sendCommand("SIZE 68");
            client.sendCommand("STOR OLD test.txt");
            client.sendCommand("SIZE 17");
            client.sendCommand("STOR APP test.txt");
            client.sendCommand("SIZE 17");
            client.sendCommand("STOR NEW test.txt");
            client.sendCommand("SIZE 17");
            client.sendCommand("RETR test-1.txt");
            client.sendCommand("SEND");
            client.sendCommand("STOR APP test-1.txt");
            client.sendCommand("SIZE 17");
            client.sendCommand("STOR OLD test-1.txt");
            client.sendCommand("SIZE 17");
            client.sendCommand("DONE");
        } catch (IOException e) {
            client.closeConnection();
        }
    }
}

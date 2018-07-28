/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.net.*;

class Server {
    public static void main(String argv[]) throws Exception {
        // Binding to port 1155 which is technically not correct for SFTP but the application needs root privileges
        // to bind to port 115.
        int port = 1155;
        ServerSocket socket = new ServerSocket(port);
        System.out.println("Server listening on port: " + String.valueOf(port));

        // Listen for incoming connections to the server.
        //noinspection InfiniteLoopStatement
        while (true) {
            Socket s = socket.accept();
            Client client = new Client(s);
            client.start();
        }
    }
}
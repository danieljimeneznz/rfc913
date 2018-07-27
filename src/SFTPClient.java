/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
 * All Rights Reserved.
 **/

import java.io.*;
import java.net.*;

class SFTPClient {

    public static void main(String argv[]) throws Exception {
        BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
        Socket socket = new Socket("localhost", 1155);
        DataOutputStream serverOut = new DataOutputStream(socket.getOutputStream());
        BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

//        outToServer.writeBytes(sentence + '\0');
        while (true) {
            System.out.println(serverIn.readLine());

            // Check to see if the server is still connected.
            if (serverIn.read() == -1) {
                SFTPClient.closeConnection(userIn, socket, serverOut, serverIn);
                return;
            }
        }
    }

    private static void closeConnection(BufferedReader userIn, Socket socket, DataOutputStream serverOut, BufferedReader serverIn) throws IOException {
        userIn.close();
        serverOut.close();
        serverIn.close();
        System.out.println("Server disconnected on socket: " + String.valueOf(socket.getPort()));
        System.out.println("Closed connection");
        socket.close();
    }
} 

/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import java.io.*;
import java.net.*;

class Client {

    public static void main(String argv[]) throws Exception {
        BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
        Socket socket = new Socket("localhost", 1155);
        DataOutputStream serverOut = new DataOutputStream(socket.getOutputStream());
        BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        while (true) {
            try {
                int c = serverIn.read();
                String s = Client.readInput(serverIn, c);
                if (s.length() > 0) {
                    System.out.println(s);
//                    serverOut.writeBytes("USER 1\0");
//                    serverOut.writeBytes("USER 3\0");
//                    serverOut.writeBytes("USER 6\0");
                    serverOut.writeBytes("TYPE a\0");
                    serverOut.writeBytes("PASS test\0");
                    serverOut.writeBytes("ACCT admin\0");
                    serverOut.writeBytes("USER 1\0");
                    serverOut.writeBytes("ACCT test\0");
                    serverOut.writeBytes("LIST F\0");
                    serverOut.writeBytes("LIST F hello\0");
                    serverOut.writeBytes("LIST F empty\0");
                    serverOut.writeBytes("LIST V\0");
                    serverOut.writeBytes("CDIR test\0");
                    serverOut.writeBytes("CDIR empty\0");
                    serverOut.writeBytes("PASS dafafa\0");
                    serverOut.writeBytes("CDIR /\0");
                    serverOut.writeBytes("DONE\0");
                }

                // Check to see if the server is still connected.
                if (c == -1) {
                    Client.closeConnection(userIn, socket, serverOut, serverIn);
                    return;
                }
            } catch (SocketException e) {
                Client.closeConnection(userIn, socket, serverOut, serverIn);
                return;
            }
        }
    }

    /**
     * Reads the input from the client until the null terminating character is sent.
     * This method also makes the thread wait for input as long as the server is connected.
     *
     * @return              the string the user has input
     * @throws IOException  an exception if error reading has occurred.
     */
    private static String readInput(BufferedReader input, int i) throws IOException {
        StringBuilder s = new StringBuilder();
        int c = i;

        while (c > 0) {
            s.append(String.valueOf((char)c));
            c = input.read();
        }

        // Remove the null terminating character from the string.
        return s.toString();
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

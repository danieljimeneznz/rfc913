/*
  Code is taken from Computer Networking: A Top-Down Approach Featuring
  the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
  All Rights Reserved.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
            // Create a new client thread and start it.
            Client client = new Client(s);
            client.start();
        }
    }

    /**
     * Get a list of users that can login to the server from the JSON file.
     * @return  the users object.
     */
    static Users getUsers() {
        try {
            Reader reader = new InputStreamReader(Server.class.getResourceAsStream("/users.json"));
            Gson gson = new GsonBuilder().create();
            Users users = gson.fromJson(reader, Users.class);
            reader.close();
            return users;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
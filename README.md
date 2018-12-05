# RFC913 Simple File Transfer Protocol
[RFC913](https://tools.ietf.org/html/rfc913) Simple File Transfer Protocol Implementation.
[GitHub](https://github.com/hydroflax/rfc913)

The following project makes use of maven to manage dependencies and run the
client/server.

For each of the respective components (i.e. server/client) there is a `mnt` directory.
This directory is treated as the root of the filesystem on the server to ensure that clients are only allowed to transfer files from within that dir.
If required, this can be modified by changing the `mountDir` in the server's `Client` class.

To easily test the application run the commands in the 'To simulate a client' section of this
README. Correct Server responses can be found in comments alongside the commands being sent in
the `client/src/main/java/TestClient.java` file.

## Prerequisites
- Java 1.8
- Maven 3

## Running
#### To run the server:
```
cd server
mvn install
mvn exec:java -Dexec.mainClass="Server"
```

#### To run the client:
```
cd client
mvn install
mvn exec:java -Dexec.mainClass="Client"
```

#### To simulate a client:
```
cd client
mvn install
mvn exec:java -Dexec.mainClass="TestClient"
```

This sends a list of commands to the server so that the responses can be easily inspected.
It also outputs the sent command to console to easily see the command being sent before the
server output is shown.

## Architecture
### Server
The server runs by continuously listening for client connections.
Once a client has connected, the server spins up a new thread and
listens for commands. When a command comes in, if it exists in the list
of supported commands then this command and context is passed to a new
command object which will handle the request for the client, replying
to the client as needed.

### Client
The client runs by first connecting to the server on the pre-determined port.
It then listens for user input on the command line and sends these commands
to the server. For some of the commands (such as 'STOR', 'RETR', etc.) the client
will run some validation on the command to ensure that the correct command is being sent
or the file a user wishes to send actually exists in the folder.

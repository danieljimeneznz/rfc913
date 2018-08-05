# RFC913 Simple File Transfer Protocol
RFC913 Simple File Transfer Protocol Implementation for COMPSYS 725.

The following project makes use of maven to manage dependencies and run the
client/server.

For each of the respective components (i.e. server/client) there is a `mnt` directory.
This directory is treated as the root of the filesystem on the server to ensure that clients are only allowed to transfer files from within that dir.
If required, this can be modified by changing the `mountDir` in the server's `Client` class.

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
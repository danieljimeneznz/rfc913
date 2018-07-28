# RFC913 Simple File Transfer Protocol
RFC913 Simple File Transfer Protocol Implementation for COMPSYS 725.

The following project makes use of maven to manage dependencies and run the
client/server.

To run the server:
```
cd server
mvn install
java -jar target/rfc913-server-1.0-SNAPSHOT.jar
```

To run the client:

```
cd client
mvn install
java -jar target/rfc913-client-1.0-SNAPSHOT.jar
```
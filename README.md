# RFC913 Simple File Transfer Protocol
RFC913 Simple File Transfer Protocol Implementation for COMPSYS 725.

The following project makes use of maven to manage dependencies and run the
client/server.

To run the server:
```
cd server
mvn install
mvn exec:java -Dexec.mainClass="Server"
```

To run the client:

```
cd client
mvn install
mvn exec:java -Dexec.mainClass="Client"
```
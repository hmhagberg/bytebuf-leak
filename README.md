# bytebuf-leak

This is an example program to reproduce ByteBuf leak in grpc-runtime and/or some related component.

## Instructions

Build the program
```
mvn clean package
```

Start the server. The server keeps running until killed.
```
./run_server.sh
```

Run the client.
```
./run_client.sh
```

There seems to be no leak after the client has been run once so run it again. After the second (and any further) execution there should be leak report in the server's output.

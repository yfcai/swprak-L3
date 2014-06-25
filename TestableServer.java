/**
 * Idea: Software is unsafe if it cannot be tested.
 *       If an interface is hard to test, then refactor it
 *       until it becomes easy to test.
 *
 * It was hard to test NaiveServer, because we must give
 * it a port number. In a test, any fixed port number could
 * be in use already, causing the test to fail when it
 * shouldn't. However, it is possible to request for a
 * ServerSocket on any available port by calling
 * `new ServerSocket(0)`. To take advantage of this mechanism,
 * we refactor `runServer` into two static methods, one of
 * which expects a ServerSocket and does not care where it
 * comes from.
 */

import java.io.*;
import java.net.*;

public class TestableServer {
    public static void runServer(int port) throws IOException {
        if (port == 0)
            throw new IllegalArgumentException("runServer(port): port must be nonzero");

        try (ServerSocket serverSocket = new ServerSocket(port);
             ) {
            runServer(serverSocket);
        }
    }

    public static void runServer(ServerSocket serverSocket) throws IOException {
        // try-with-resources
        try (// block until a client connects
             Socket socket = serverSocket.accept();

             // read what the client sends
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

             // send things to client
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true /* autoflush */);

             ){

            String line;
            int sum = 0;

            while ((line = reader.readLine()).length() > 0) {
                System.out.println("received: " + line);
                sum += Integer.parseInt(line);
            }

            System.out.println("sending sum = " + sum);
            writer.println(sum);

            line = reader.readLine();
            System.out.println(String.format("client says %d is %s", sum, line));
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1)
            System.err.println("Usage: java Server <port-number>");
        else
            runServer(Integer.parseInt(args[0]));
    }
}


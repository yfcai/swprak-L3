/**
 * Server
 * - serves 1 client during its lifetime
 * - reads integers, one per line, until an empty line
 * - computes and writes the sum
 * - reads whether the sum is positive or negative
 *
 *
 * Client/server dialogue example
 *
 * server: listens at port 1234
 * client: connects to localhost at port 1234
 * client: sends the line "1"
 * client: sends the line "2"
 * client: sends the line "3"
 * client: sends the line "4"
 * client: sends an empty line
 * server: sends the line "10"
 * client: sends the line "positive"
 * client: closes the socket connection
 * server: closes the socket connection
 */

import java.io.*;  // IOException, InputStream, BufferedReader, PrintWriter
import java.net.*; // ServerSocket

public class NaiveServer {
    public static void runServer(int port) throws IOException {
        if (port == 0)
            throw new IllegalArgumentException("runServer(port): port must be nonzero");

        // try-with-resources
        try (// bind & listen at port
             ServerSocket serverSocket = new ServerSocket(port);

             // block until a client connects
             Socket socket = serverSocket.accept();

             // read what the client sends
             BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

             // send things to client
             PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true /* autoflush */);

             ){

            String line;
            int sum = 0;

            while ((line = fromClient.readLine()).length() > 0) {
                System.out.println("received: " + line);
                sum += Integer.parseInt(line);
            }

            System.out.println("sending sum = " + sum);
            toClient.println(sum);
            if (toClient.checkError())
                throw new IOException("error on send");

            line = fromClient.readLine();
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

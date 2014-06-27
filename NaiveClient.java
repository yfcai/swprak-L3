/**
 * Client
 * - connects to 1 server during its lifetime
 * - sends integers, one per line, terminated by a blank line
 * - receives the sum from server
 * - tells server whether the sum is positive, negative or neither
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

import java.io.*;
import java.net.*;

public class NaiveClient {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java Client <hostname> <port-number>");
            System.exit(1);
        }

        // server is identified by a hostname and a port number
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        // try-with-resources is Java 7 only!
        try (// create a socket and connect it to server
             Socket clientSocket = new Socket(hostname, port);

             // read what server sends
             BufferedReader fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

             // send things to server
             PrintWriter toServer = new PrintWriter(clientSocket.getOutputStream(), true /* autoflush */);

             ){

            System.out.println("Connection established.");
            System.out.println("Please input one number per line, terminated by a blank line.");

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String line;

            // send all user input to the server
            do {
                line = stdin.readLine();
                System.out.println("sending: " + line);
                toServer.println(line); // PrintWriter catches IOException thrown in underlying stream!
                if (toServer.checkError()) // thus we must manually check for possible errors
                    throw new RuntimeException("error on send");
            }
            while (! "".equals(line));

            // get the sum from the server
            int sum = Integer.parseInt(fromServer.readLine());

            // reply whether sum is positive, negative or neither
            String reply = sum > 0 ? "positive" : sum < 0 ? "negative" : "neither positive nor negative";

            System.out.println(String.format("the sum %d is %s", sum, reply));

            toServer.println(reply);
            if (toServer.checkError())
                throw new IOException("error on send");
        }
    }
}

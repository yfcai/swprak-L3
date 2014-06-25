/**
 * It was hard to test NaiveClient, because it requires user
 * interaction to run. Tests must be automatic; they can only use
 * the parts without user interaction. We extract all client
 * behavior independent of user interaction in stateful instances
 * of `TestableClient`, and put all the user interactions in the
 * static `main` method.
 */


import java.io.*;
import java.net.*;

public class TestableClient {

    /**
     * Constructing a stateful client.
     *
     *     Socket clientSocket = new Socket(serverHostName, serverPortNumber);
     *     TestableClient client = new TestableClient(clientSocket);
     *     client.send(1);
     *     client.send(2);
     *     client.send(3);
     *     client.send(4);
     *     client.conclude();
     *
     * @param socket	a socket already connected to some server
     */
    TestableClient(Socket socket) throws IOException {
        this.socket = socket;
        this.fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.toServer = new PrintWriter(socket.getOutputStream(), true /* autoflush */);
    }

    Socket socket;
    BufferedReader fromServer;
    PrintWriter toServer;

    /**
     * Send a line to server.
     * @param line	the line to be sent
     */
    public void send(Object line) throws IOException {
        toServer.println(line.toString());
        if (toServer.checkError())
            throw new IOException(String.format("got error while trying to send the following line:\n\n%s\n\n", line));
    }

    /**
     * Conclude the protocol: send an empty, read server response, and reply.
     *
     * @return String.format("the sum %d is %s", sumReceivedFromServer, lastReplyOfClient);
     *         Examples:
     *         - "the sum 10 is positive"
     *         - "the sum -5 is negative"
     *         - "the sum 0 is neither positive nor negative"
     */
    public String conclude() throws IOException {
        send("");
        int sum = Integer.parseInt(fromServer.readLine());
        String reply = sum > 0 ? "positive" : sum < 0 ? "negative" : "neither positive nor negative";
        send(reply);
        return String.format("the sum %d is %s", sum, reply);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java Client <hostname> <port-number>");
            System.exit(1);
        }

        // server is identified by a hostname and a port number
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        // try-with-resources is Java 7 only!
        try (Socket clientSocket = new Socket(hostname, port)){

            TestableClient client = new TestableClient(clientSocket);

            System.out.println("Connection established.");
            System.out.println("Please input one number per line, terminated by a blank line.");

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String line;

            // send all user input to the server
            while (! "".equals(line = stdin.readLine())) {
                System.out.println("sending: " + line);
                client.send(line);
            }

            System.out.println(client.conclude());
        }
    }
}

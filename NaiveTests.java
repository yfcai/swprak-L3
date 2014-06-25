/**
 * JUnit test for server on input 1, 2, 3, 4
 *
 * Compilation:
 *   # put junit-4.11.jar and hamcrest-core-1.3.jar in this directory
 *   javac -cp .:* *.java
 *
 * Execution:
 *   java -cp .:* org.junit.runner.JUnitCore Tests
 */

import static org.junit.Assert.*;

import org.junit.Test;

import java.net.*;
import java.io.*;

public class NaiveTests {
    public static final String localhost = "localhost";

    @Test
    public void testServerOn1234() throws IOException, InterruptedException {
        System.out.println("Testing TestableServer on 1, 2, 3, 4:");

        // get a server socket on any available port
        try (ServerSocket serverSocket = new ServerSocket(0)) {

            // get the port assigned to us
            int port = serverSocket.getLocalPort();

            // start the server in another thread
            Thread serverThread = new Thread() {
                    public void run() {
                        try {
                            TestableServer.runServer(serverSocket);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };

            serverThread.start();

            // Pretend that we are a client, send a fixed set of responses,
            // and verify traffic from server

            // get a client socket
            try (Socket clientSocket = new Socket(localhost, port);
                 BufferedReader fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter toServer = new PrintWriter(clientSocket.getOutputStream(), true /* autoflush */);

                 ){

                // send 1, 2, 3, 4 to the server
                toServer.println(1);
                toServer.println(2);
                toServer.println(3);
                toServer.println(4);
                toServer.println();

                assertEquals("10", fromServer.readLine());

                toServer.println("positive");

                serverThread.join();
            }
        }
    }
}

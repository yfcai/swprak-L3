/**
 * Abstract over relevant input/output data in NaiveTests
 * so that we can write more test cases easily.
 */

import static org.junit.Assert.*;

import org.junit.Test;
import org.apache.commons.lang3.StringUtils;

import java.net.*;
import java.io.*;

public class ModularTests {
    public static final String localhost = "localhost";

    @Test
    public void test1234() throws Exception {
        testServerAndClient(new Integer[] { 1, 2, 3, 4 }, "10", "positive");
    }

    @Test
    public void testEmptyInput() throws Exception {
        testServerAndClient(new Integer[] {}, "0", "neither positive nor negative");
    }

    @Test
    public void testNegative() throws Exception {
        testServerAndClient(new Integer[] { 1, -2, 3, -4, 5, -6, 7, -8 }, "-4", "negative");
    }

    public void testServerAndClient(Integer[] clientInput, String serverResponse, String clientReply)
        throws IOException, InterruptedException
    {
        testServer(clientInput, serverResponse, clientReply);
        testClient(clientInput, serverResponse, clientReply);
    }

    public void testServer(Integer[] clientInput, String serverResponse, String clientReply)
        throws IOException, InterruptedException
    {
        System.out.println(String.format("\nTesting TestableServer on [%s]",
                                         StringUtils.join(clientInput, ", ")));

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

                // send client input to server
                for (Integer input : clientInput)
                    toServer.println(input);

                // If this line is commented out, then deadlock will occur.
                // Press ctrl+\ (SIGQUIT) to dump stack traces of all threads.
                toServer.println();

                assertEquals(serverResponse, fromServer.readLine());

                toServer.println(clientReply);

                serverThread.join();
            }
        }
    }

    public void testClient(final Integer[] clientInput, String serverResponse, String expectedClientReply)
        throws IOException, InterruptedException
    {
        System.out.println(String.format("\nTesting TestableClient on [%s]",
                                         StringUtils.join(clientInput, ", ")));
        System.out.println("server response = " + serverResponse);
        System.out.println("expected reply  = " + expectedClientReply);

        // server mock-up
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            // port number, to be communicated to the client thread
            // we avoid concurrency issues by sharing only final, primitive data
            final int port = serverSocket.getLocalPort();

            // start client in a separate thread
            Thread clientThread = new Thread() {
                    public void run() {
                        try (Socket socket = new Socket(localhost, port)) {
                            TestableClient client = new TestableClient(socket);
                            for (Integer input : clientInput) {
                                client.send(input);
                            }
                            client.conclude();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };

            // must start client before calling serverSocket.accept(),
            // otherwise the call to .accept() will block indefinitely.
            clientThread.start();

            // send server response, compare client reply
            try (Socket clientSocket = serverSocket.accept()) {
                new PrintWriter(clientSocket.getOutputStream(), true).println(serverResponse);
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // check that client sends the input back
                for (Integer input : clientInput)
                    assertEquals(input.toString(), fromClient.readLine());

                // check that client terminates input with an empty line
                assertEquals("", fromClient.readLine());

                // check whether reply is as expected
                assertEquals(expectedClientReply, fromClient.readLine());
            }

            clientThread.join();
        }
    }
}

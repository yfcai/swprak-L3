/**
 * Using @Before and @After annotations of JUnit to make
 * ModularTests for the server easier to write. Tests
 * for the client can be structured in this way, too.
 */

import static org.junit.Assert.*;

import org.junit.*;
import org.apache.commons.lang3.StringUtils;

import java.net.*;
import java.io.*;


public class AnnotatedServerTests {
    // final variable for constant to guard against typo
    final static String localhost = "localhost";

    // use fields to store resources necessary for tests
    ServerSocket   serverSocket;
    int            port;
    Thread         serverThread;
    Socket         clientSocket;
    BufferedReader fromServer;
    PrintWriter    toServer;

    // the method annotated with @Before is executed before each test
    @Before
    public void setUpConnection() throws IOException {

        // bind a server socket to an unspecified available port
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();

        // start the server in a thread
        serverThread = new Thread() {
                public void run() {
                    try {
                        TestableServer.runServer(serverSocket);
                    }
                    catch (IOException e) {
                        System.err.println("IOException caught in server thread:");
                        e.printStackTrace();
                    }
                }
            };
        serverThread.start();

        // establish connection with the server
        clientSocket = new Socket(localhost, port);
        fromServer   = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        toServer     = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    // the method annotated with @After is executed after each test
    @After
    public void disposeResources() throws IOException, InterruptedException {

        // close the server socket
        serverSocket.close();
        serverSocket = null;

        // assign `port` to some illegal value that will trigger
        // an exception as soon as it's used to create a socket,
        // so that we will notice it immediately when `port` is
        // used without being initialized first.
        port = -1;

        // close the client socket
        clientSocket.close();
        clientSocket = null;
        fromServer   = null;
        toServer     = null;

        // wait for the server thread to die
        serverThread.join();
        serverThread = null;
    }

    // send a line to server and check for errors
    void sendln(Object line) throws IOException {
        toServer.println(line);
        if (toServer.checkError())
            throw new IOException("error on send");
    }

    // the workflow of ModularTests's server tests
    void testServer(Integer[] clientInput, String serverResponse, String clientReply)
        throws IOException
    {
        System.out.println("\nTesting server on fixed input");
        for (Integer input : clientInput)
            sendln(input);
        sendln("");
        assertEquals(serverResponse, fromServer.readLine());
        sendln(clientReply);
    }

    // test cases in ModularTests
    @Test
    public void test1234() throws Exception {
        testServer(new Integer[] { 1, 2, 3, 4 }, "10", "positive");
    }

    @Test
    public void testEmptyInput() throws Exception {
        testServer(new Integer[] {}, "0", "neither positive nor negative");
    }

    @Test
    public void testNegative() throws Exception {
        testServer(new Integer[] { 1, -2, 3, -4, 5, -6, 7, -8 }, "-4", "negative");
    }
}

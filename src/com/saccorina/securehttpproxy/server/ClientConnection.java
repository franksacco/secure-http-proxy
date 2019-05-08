package com.saccorina.securehttpproxy.server;

import com.saccorina.securehttpproxy.ConnectionCipher;
import com.saccorina.securehttpproxy.Logger;
import com.saccorina.securehttpproxy.Utility;
import com.saccorina.securehttpproxy.exception.CipherException;

import java.io.*;
import java.net.Socket;

/**
 * Handler class for the connection between proxy-client and proxy-server.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
public class ClientConnection extends Thread {

    private final Logger logger = Logger.getInstance();

    /**
     * The socket used for the connection.
     */
    private Socket socket;

    /**
     * The cipher used to encrypt communication.
     */
    private ConnectionCipher cipher;

    /**
     * Initialize a connection to a client.
     *
     * @param clientSocket The socket used for the connection to a client.
     */
    ClientConnection(Socket clientSocket, ConnectionCipher cipher) {
        this.socket = clientSocket;
        this.cipher = cipher;

        logger.log("Connection established with " + this.socket.getInetAddress());
    }

    /**
     * {@inheritDoc}
     */
    public void run()
    {
        try {
            byte[] request  = this.getRequestFromProxy(this.socket.getInputStream());
            byte[] response = this.createResponse(request);

            String message = Utility.bytesToHexString(
                    this.cipher.encrypt(Utility.hexStringToBytes("11223344556677881122334455667788"), response)
            );

            System.out.println("--- START ENCRYPTED RESPONSE ---");
            System.out.println(message);
            System.out.println("--- END ENCRYPTED RESPONSE ---");

            this.socket.getOutputStream().write(message.getBytes());

        } catch (IOException | CipherException e) {
            logger.error("Error in proxy-server communication", e);

        } finally {
            try {
                this.socket.close();
                logger.log("Proxy-server connection closed");
            } catch (IOException e) {
                logger.error("Error closing proxy-server socket", e);
            }
        }
    }

    /**
     * Retrieve the proxy request from the input stream.
     *
     * @param inputStream The input stream of the server socket.
     * @return Returns the proxy request in bytes.
     *
     * @throws IOException if an I/O error occurs.
     */
    private byte[] getRequestFromProxy(InputStream inputStream) throws IOException {

        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder requestBuilder = new StringBuilder();

        String inputLine;
        while ((inputLine = input.readLine()) != null) {
            requestBuilder.append(inputLine).append("\r\n");
            if (inputLine.isEmpty()) break;
        }

        String request = requestBuilder.toString();

        System.out.println("--- START PROXY REQUEST ---");
        System.out.print(request);
        System.out.println("--- END PROXY REQUEST ---");

        return request.getBytes();
    }

    /**
     * Creates the server response.
     *
     * @return Returns the array of bytes representing the server response.
     *
     * @throws IOException if an I/O error occurs.
     */
    private byte[] createResponse(byte[] request) throws IOException {

        Socket serverSocket = new Socket("localhost", 80);

        PrintWriter output = new PrintWriter(serverSocket.getOutputStream(), true);
        output.println(new String(request));

        BufferedReader input = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();

        String inputLine;
        int contentLength = 0;
        while ((inputLine = input.readLine()) != null)
        {
            if (inputLine.length() >= 17 &&
                    inputLine.substring(0, 14).equals("Content-Length")) {
                // this line is the "Content-Length" header
                contentLength = Integer.parseInt(inputLine.substring(16));
            }
            responseBuilder.append(inputLine).append("\r\n");

            if (inputLine.isEmpty()) {
                // actual line is an empty line that divides headers and body
                if (contentLength > 0) {
                    // now read the body of the response
                    char[] body = new char[contentLength];
                    input.read(body, 0, contentLength);
                    responseBuilder.append(body);
                }
                break;
            }
        }
        String response = responseBuilder.toString();

        System.out.println("--- START SERVER RESPONSE ---");
        System.out.println(response);
        System.out.println("--- END SERVER RESPONSE ---");

        return response.getBytes();
    }

}

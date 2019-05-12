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
public class ServerProxyConnection extends Thread {

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
    ServerProxyConnection(Socket clientSocket, ConnectionCipher cipher) {
        this.socket = clientSocket;
        this.cipher = cipher;

        this.log("Connection established");
    }

    /**
     * {@inheritDoc}
     */
    public void run()
    {
        try {
            byte[] encryptedRequest = this.getRequestFromProxy(this.socket.getInputStream());
            byte[] request = this.cipher.decrypt(encryptedRequest);

            this.log("HTTP request received and decrypted (" + request.length + " bytes)");
            if (ServerProxy.DEBUG) {
                System.out.println("--- SERVER REQUEST ---");
                System.out.print(new String(request));
                System.out.println("--- SERVER REQUEST ---");
            }

            byte[] response = this.createResponse(request);
            byte[] encryptedResponse = this.cipher.encrypt(response);
            encryptedResponse = Utility.bytesToHexString(encryptedResponse)
                    .concat("\r\n") // Needed to terminate message in stream
                    .getBytes();

            this.log("HTTP response encrypted and sent (" + response.length + " bytes)");
            if (ServerProxy.DEBUG) {
                System.out.println("--- SERVER RESPONSE ---");
                System.out.print(new String(response));
                System.out.println("--- SERVER RESPONSE ---");
            }

            this.socket.getOutputStream().write(encryptedResponse);

        } catch (IOException | CipherException e) {
            logger.error("Error in proxy-server communication", e);

        } finally {
            try {
                this.socket.close();
                this.log("Connection closed");
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
            if (inputLine.isEmpty()) break;
            requestBuilder.append(inputLine);
        }

        return Utility.hexStringToBytes(requestBuilder.toString());
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
                    responseBuilder.append(body).append("\r\n");
                }
                break;
            }
        }

        return responseBuilder.toString().getBytes();
    }

    /**
     * Log some message.
     *
     * @param message The message to log.
     */
    private void log(String message) {
        logger.log("[ServerProxy@" + this.socket.getInetAddress().getHostAddress() + "] " + message);
    }

}

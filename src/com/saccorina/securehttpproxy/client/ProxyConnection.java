package com.saccorina.securehttpproxy.client;

import com.saccorina.securehttpproxy.ConnectionCipher;
import com.saccorina.securehttpproxy.Logger;
import com.saccorina.securehttpproxy.Utility;
import com.saccorina.securehttpproxy.exception.CipherException;

import java.io.*;
import java.net.Socket;

/**
 * Handler class for the connection between real-client and proxy-client.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
public class ProxyConnection extends Thread {

    private final Logger logger = Logger.getInstance();

    /**
     * The socket used for the proxy connection with the local client.
     */
    private Socket socket;

    /**
     * The cipher used to encrypt communication.
     */
    private ConnectionCipher cipher;

    /**
     * Initialize a proxy connection.
     *
     * @param proxySocket The socket used for the proxy connection.
     * @param cipher The cipher used to encrypt communication.
     */
    ProxyConnection(Socket proxySocket, ConnectionCipher cipher) {
        this.socket = proxySocket;
        this.cipher = cipher;

        logger.log("Proxy connection established with " + this.socket.getInetAddress());
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        try {
            byte[] request  = this.getRequestFromClient(this.socket.getInputStream());
            byte[] response = this.sendRequestToServer(request);

            byte[] message = this.cipher.decrypt(
                    Utility.hexStringToBytes("11223344556677881122334455667788"),
                    response
            );

            System.out.println("--- START DECRYPTED RESPONSE ---");
            System.out.println(new String(message));
            System.out.println("--- END DECRYPTED RESPONSE ---");

            this.socket.getOutputStream().write(message);

        } catch (IOException | CipherException e) {
            logger.error("Error in client-proxy communication", e);

        } finally {
            try {
                this.socket.close();
                logger.log("Proxy connection closed");

            } catch (IOException e) {
                logger.error("Error closing proxy socket", e);
            }
        }
    }

    /**
     * Retrieve the client HTTP request from the input stream.
     *
     * @param inputStream The input stream of the server socket.
     * @return Returns the client HTTP request in bytes.
     *
     * @throws IOException if an I/O error occurs.
     */
    private byte[] getRequestFromClient(InputStream inputStream) throws IOException {

        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder requestBuilder = new StringBuilder();

        String inputLine;
        while ((inputLine = input.readLine()) != null) {
            // TODO read eventually the body
            requestBuilder.append(inputLine).append("\r\n");
            if (inputLine.isEmpty()) break;
        }
        String request = requestBuilder.toString();

        System.out.println("--- START CLIENT REQUEST ---");
        System.out.print(request);
        System.out.println("--- END CLIENT REQUEST ---");

        return request.getBytes();
    }

    /**
     * Sends request to the server and returns the response.
     *
     * @param request The client HTTP request in bytes.
     * @return Returns the server response in bytes.
     *
     * @throws IOException if an I/O error occurs.
     */
    private byte[] sendRequestToServer(byte[] request) throws IOException {

        Socket serverSocket = new Socket("localhost", 5000);

        PrintWriter output = new PrintWriter(serverSocket.getOutputStream(), true);
        output.println(new String(request));

        BufferedReader input = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();

        String inputLine;
        while ((inputLine = input.readLine()) != null) {
            responseBuilder.append(inputLine);
        }
        String response = responseBuilder.toString();

        System.out.println("--- START SERVER RESPONSE ---");
        System.out.println(response);
        System.out.println("--- END SERVER RESPONSE ---");

        return Utility.hexStringToBytes(response);
    }

}

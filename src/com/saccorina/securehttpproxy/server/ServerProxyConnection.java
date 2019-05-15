package com.saccorina.securehttpproxy.server;

import com.saccorina.securehttpproxy.ConnectionCipher;
import com.saccorina.securehttpproxy.HttpMessageReader;
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

    /**
     * The application logger.
     */
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
     * @param socket The socket used for the connection to a client.
     * @param cipher The cipher configured to encrypt or decrypt HTTP messages.
     */
    ServerProxyConnection(Socket socket, ConnectionCipher cipher) {
        this.socket = socket;
        this.cipher = cipher;

        this.log("Connection established");
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        try {
            byte[] encryptedRequest = this.getRequestFromProxy(this.socket.getInputStream());
            byte[] request = this.cipher.decrypt(encryptedRequest);

            if (ServerProxy.DEBUG) {
                System.out.println("--- SERVER REQUEST ---");
                System.out.println(new String(request));
                System.out.println("--- SERVER REQUEST ---");
            }
            this.log("HTTP request received and decrypted (" + request.length + " bytes)");

            byte[] response = this.retrieveResponse(request);
            byte[] encryptedResponse = this.cipher.encrypt(response);
            encryptedResponse = Utility.bytesToHexString(encryptedResponse)
                    .concat("\n\n") // Needed to terminate message in stream
                    .getBytes();

            if (ServerProxy.DEBUG) {
                System.out.println("--- SERVER RESPONSE ---");
                System.out.println(new String(response));
                System.out.println("--- SERVER RESPONSE ---");
            }
            this.log("HTTP response encrypted and sent (" + response.length + " bytes)");

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
     * @param in The input stream of the server socket.
     * @return Returns the proxy request in bytes.
     *
     * @throws IOException if an I/O error occurs.
     */
    private byte[] getRequestFromProxy(InputStream in) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            if (line.isEmpty()) break;
            requestBuilder.append(line);
        }
        return Utility.hexStringToBytes(requestBuilder.toString());
    }

    /**
     * Retrieves the HTTP response from the final server.
     *
     * @param request The HTTP request from the initial client.
     * @return Returns the server response as byte array.
     *
     * @throws IOException if an I/O error occurs.
     */
    private byte[] retrieveResponse(byte[] request) throws IOException {
        // connect to the final server
        Socket socket = new Socket("localhost", 80);

        // send the client request
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(request);
        outputStream.flush();

        // receive the server response
        InputStream inputStream = socket.getInputStream();
        HttpMessageReader responseReader = new HttpMessageReader(inputStream);
        return responseReader.getMessage();
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

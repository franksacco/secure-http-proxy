package com.saccorina.securehttpproxy.client;

import com.saccorina.securehttpproxy.ConnectionCipher;
import com.saccorina.securehttpproxy.HttpMessageReader;
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
public class ClientProxyConnection extends Thread {

    /**
     * The application logger.
     */
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
     * The server host.
     */
    private final String serverHost;
    /**
     * The server port.
     */
    private final int serverPort;

    /**
     * Initialize a proxy connection.
     *
     * @param proxySocket The socket used for the proxy connection.
     * @param cipher The cipher used to encrypt communication.
     * @param serverHost The server host.
     * @param serverPort The server port.
     */
    ClientProxyConnection(Socket proxySocket,
                          ConnectionCipher cipher,
                          String serverHost,
                          int serverPort)
    {
        this.socket     = proxySocket;
        this.cipher     = cipher;
        this.serverHost = serverHost;
        this.serverPort = serverPort;

        this.log("Connection established");
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        try {
            // retrieve the request from the client
            HttpMessageReader requestReader = new HttpMessageReader(this.socket.getInputStream());
            String requestedHost = requestReader.getHost();
            byte[] request = requestReader.getMessage();
            if (ClientProxy.DEBUG) {
                System.out.println("--- CLIENT REQUEST ---");
                System.out.println(new String(request));
                System.out.println("--- CLIENT REQUEST ---");
            }

            byte[] response;
            if (requestedHost != null && requestedHost.equals(this.serverHost)) {
                byte[] encryptedRequest = this.cipher.encrypt(request);
                encryptedRequest = Utility.bytesToHexString(encryptedRequest)
                        .concat("\n\n") // needed to terminate message in output stream
                        .getBytes();

                this.log("HTTP request encrypted and sent (" + request.length + " bytes)");

                byte[] encryptedResponse = this.sendRequestToServer(encryptedRequest);
                response = this.cipher.decrypt(encryptedResponse);
                this.log("HTTP response received and decrypted (" + response.length + " bytes)");

            } else {
                this.log("HTTP request sent to external host \"" + requestedHost + "\"");
                response = this.sendRequestToExternalServer(requestedHost, request);
                this.log("HTTP response received from external host \"" + requestedHost + "\"");
            }

            if (ClientProxy.DEBUG) {
                System.out.println("--- SERVER RESPONSE ---");
                System.out.println(new String(response));
                System.out.println("--- SERVER RESPONSE ---");
            }

            this.socket.getOutputStream().write(response);

        } catch (IOException | CipherException e) {
            logger.error("Error in client-proxy communication", e);

        } finally {
            try {
                this.socket.close();
                this.log("Connection closed");

            } catch (IOException e) {
                logger.error("Error closing proxy socket", e);
            }
        }
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
        // connect to the proxy server
        Socket socket = new Socket(this.serverHost, this.serverPort);

        // send the client request
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(request);
        outputStream.flush();

        // receive the server response
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            if (line.isEmpty()) break;
            responseBuilder.append(line);
        }
        return Utility.hexStringToBytes(responseBuilder.toString());
    }

    /**
     * Sends request to an external server and returns the response.
     *
     * @param host The external host where to send the request.
     * @param request The client HTTP request in bytes.
     * @return Returns the server response in bytes.
     *
     * @throws IOException if an I/O error occurs.
     */
    private byte[] sendRequestToExternalServer(String host, byte[] request) throws IOException {
        // connect to the external server
        Socket socket = new Socket(host, 80);

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
        logger.log("[ClientProxy@" + this.socket.getInetAddress().getHostAddress() + "] " + message);
    }

}

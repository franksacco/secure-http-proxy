package com.saccorina.securehttpproxy.client;

import com.saccorina.securehttpproxy.ConnectionCipher;
import com.saccorina.securehttpproxy.Logger;
import com.saccorina.securehttpproxy.Utility;
import com.saccorina.securehttpproxy.exception.CipherException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Handler class for the connection between real-client and proxy-client.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
public class ClientProxyConnection extends Thread {

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
     * The target host of the client HTTP request.
     */
    private String requestedHost = "localhost";

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
            byte[] request  = this.getRequestFromClient(this.socket.getInputStream());
            if (ClientProxy.DEBUG) {
                System.out.println("--- CLIENT REQUEST ---");
                System.out.print(new String(request));
                System.out.println("--- CLIENT REQUEST ---");
            }

            byte[] response;
            if (this.requestedHost.equals("localhost")) {
                byte[] encryptedRequest = this.cipher.encrypt(request);
                encryptedRequest = Utility.bytesToHexString(encryptedRequest)
                        .concat("\r\n") // needed to terminate message in stream
                        .getBytes();

                this.log("HTTP request encrypted and sent (" + request.length + " bytes)");

                byte[] encryptedResponse = this.sendRequestToServer(encryptedRequest);
                response = this.cipher.decrypt(encryptedResponse);
                this.log("HTTP response received and decrypted (" + response.length + " bytes)");

            } else {
                this.log("HTTP request sent to external host \"" + this.requestedHost + "\"");
                response = this.sendRequestToExternalServer(request);
                this.log("HTTP response received from external host \"" + this.requestedHost + "\"");
            }

            if (ClientProxy.DEBUG) {
                System.out.println("--- SERVER RESPONSE ---");
                System.out.print(new String(response));
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
        int contentLength = 0;
        while ((inputLine = input.readLine()) != null)
        {
            if (inputLine.length() >= 7 && inputLine.substring(0, 4).equals("Host")) {
                // this line is the "Host" header
                this.requestedHost = inputLine.substring(6);
                if (this.requestedHost.contains(":")) {
                    // remove port if set
                    this.requestedHost = this.requestedHost.split(":")[0];
                }

            } else if (inputLine.length() >= 18 && inputLine.substring(0, 15).equals("Accept-Encoding")) {
                // todo not remove the encoding header
                continue;

            } else if (inputLine.length() >= 17 && inputLine.substring(0, 14).equals("Content-Length")) {
                // this line is the "Content-Length" header
                contentLength = Integer.parseInt(inputLine.substring(16));
            }
            requestBuilder.append(inputLine).append("\r\n");

            if (inputLine.isEmpty()) {
                // actual line is the empty line that divides headers and body
                if (contentLength > 0) {
                    // now read the body of the response
                    char[] body = new char[contentLength];
                    input.read(body, 0, contentLength);
                    requestBuilder.append(body);
                }
                break;
            }
        }

        return requestBuilder.toString().getBytes();
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

        Socket serverSocket = new Socket(this.serverHost, this.serverPort);

        PrintWriter output = new PrintWriter(serverSocket.getOutputStream(), true);
        output.println(new String(request));

        BufferedReader input = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();

        String inputLine;
        while ((inputLine = input.readLine()) != null) {
            if (inputLine.isEmpty()) break;
            responseBuilder.append(inputLine);
        }

        return Utility.hexStringToBytes(responseBuilder.toString());
    }

    /**
     * Sends request to an external server and returns the response.
     *
     * @param request The client HTTP request in bytes.
     * @return Returns the server response in bytes.
     *
     * @throws IOException if an I/O error occurs.
     */
    private byte[] sendRequestToExternalServer(byte[] request) throws IOException {

        Socket serverSocket = new Socket(this.requestedHost, 80);

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

        return responseBuilder.toString().getBytes(StandardCharsets.UTF_8);
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

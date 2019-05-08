package com.saccorina.securehttpproxy.server;

import com.saccorina.securehttpproxy.ConnectionCipher;
import com.saccorina.securehttpproxy.Logger;
import com.saccorina.securehttpproxy.exception.CipherException;
import com.saccorina.securehttpproxy.exception.SocketException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class provide a Secure HTTP Proxy in the server-side.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
class Server {

    private final Logger logger = Logger.getInstance();

    /**
     * The port number for the proxy server.
     */
    private int serverPort;

    /**
     * Initialize the server.
     */
    Server() {
        this(5000);
    }

    /**
     * Initialize the server.
     *
     * @param port The port number of the server.
     */
    Server(int port) {
        this.serverPort = port;
    }

    /**
     * Starts the server.
     *
     * @throws SocketException if a socket error occurs.
     */
    void startServer() throws SocketException, CipherException {
        try {
            ServerSocket server = new ServerSocket(this.serverPort);
            server.setReuseAddress(true);

            logger.log("Server started on localhost, port " + this.serverPort);

            while (true) {
                logger.log("Waiting for a new connection...");
                Socket clientSocket = server.accept();

                ClientConnection connection = new ClientConnection(clientSocket, new ConnectionCipher());
                connection.start();
            }

        } catch (IOException e) {
            throw new SocketException(e.getMessage(), e);
        }
    }

}

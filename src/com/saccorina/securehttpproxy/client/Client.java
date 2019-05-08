package com.saccorina.securehttpproxy.client;

import com.saccorina.securehttpproxy.ConnectionCipher;
import com.saccorina.securehttpproxy.Logger;
import com.saccorina.securehttpproxy.exception.CipherException;
import com.saccorina.securehttpproxy.exception.SocketException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class provide a Secure HTTP Proxy in the client-side.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
class Client {

    private final Logger logger = Logger.getInstance();

    /**
     * The port number for the proxy server.
     */
    private int proxyServerPort;

    /**
     * Initialize the secure proxy client.
     */
    Client() {
        this(4000);
    }

    /**
     * Initialize the secure proxy client.
     *
     * @param port The port number of the proxy server.
     */
    Client(int port) {
        this.proxyServerPort = port;
    }

    /**
     * Starts the proxy server.
     *
     * @throws SocketException if a socket error occurs.
     */
    void startProxyServer() throws SocketException, CipherException {
        try {
            ServerSocket proxyServer = new ServerSocket(this.proxyServerPort);
            proxyServer.setReuseAddress(true);

            logger.log("[Proxy-Server] Started on localhost, port " + this.proxyServerPort);

            while (true) {
                logger.log("[Proxy-Server] Waiting for a new connection...");
                Socket proxySocket = proxyServer.accept();

                ProxyConnection connection = new ProxyConnection(proxySocket, new ConnectionCipher());
                connection.start();
            }

        } catch (IOException e) {
            throw new SocketException(e.getMessage(), e);
        }
    }

}
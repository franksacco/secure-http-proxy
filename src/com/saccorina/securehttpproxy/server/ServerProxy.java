package com.saccorina.securehttpproxy.server;

import com.saccorina.securehttpproxy.ConnectionCipher;
import com.saccorina.securehttpproxy.Logger;
import com.saccorina.securehttpproxy.exception.CipherException;
import com.saccorina.securehttpproxy.exception.SocketException;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class provide a Secure HTTP Proxy in the server-side.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
class ServerProxy {

    static final boolean DEBUG = false;

    /**
     * The application logger.
     */
    private static final Logger logger = Logger.getInstance();

    /**
     * The port number for the proxy server.
     */
    private final int port;

    /**
     * The secret key used for encryption/decryption.
     */
    private byte[] secretKey;

    /**
     * Initialize the secure proxy server.
     *
     * @param port The port number for the proxy server.
     *
     * @throws CipherException if an error occurs during key generation.
     * @throws SocketException if an I/O error occurs during the exchange.
     */
    private ServerProxy(int port) throws CipherException, SocketException {
        this.port = port;

        DHKeyExchange dhKeyExchange = new DHKeyExchange();
        dhKeyExchange.performsKeyExchange(port);
        this.secretKey = dhKeyExchange.getSecretKey();
    }

    /**
     * Starts the server.
     *
     * @throws SocketException if a socket error occurs.
     */
    private void startServer() throws SocketException, CipherException {
        try {
            ServerSocket server = new ServerSocket(port);
            server.setReuseAddress(true);
            logger.log("[ServerProxy] Started on localhost, port " + port);

            ConnectionCipher cipher = new ConnectionCipher();
            cipher.setSecretKey(new SecretKeySpec(this.secretKey, 0, 16, "AES"));

            while (true) {
                logger.log("[ServerProxy] Waiting for a new connection...");
                Socket clientSocket = server.accept();

                ServerProxyConnection connection = new ServerProxyConnection(clientSocket, cipher);
                connection.start();
            }
        } catch (IOException e) {
            throw new SocketException(e.getMessage(), e);
        }
    }

    /**
     * ServerProxy application entry-point.
     *
     * @param args Console arguments.
     */
    public static void main(String[] args) {
        try {
            ServerProxy server = new ServerProxy(4321);
            server.startServer();

        } catch (Throwable e) {
            logger.error("FATAL ERROR", e);
        }
    }

}

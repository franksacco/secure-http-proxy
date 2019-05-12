package com.saccorina.securehttpproxy.client;

import com.saccorina.securehttpproxy.ConnectionCipher;
import com.saccorina.securehttpproxy.Logger;
import com.saccorina.securehttpproxy.exception.CipherException;
import com.saccorina.securehttpproxy.exception.SocketException;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class provide a Secure HTTP Proxy in the client-side.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
class ClientProxy {

    static final boolean DEBUG = true;

    private final Logger logger = Logger.getInstance();

    /**
     * The server host.
     */
    private final String serverHost;
    /**
     * The server port.
     */
    private final int serverPort;

    /**
     * The secret key used for encryption/decryption.
     */
    private byte[] secretKey;

    /**
     * Initialize the secure proxy client.
     *
     * @param serverHost The server port.
     * @param serverPort The server port.
     *
     * @throws CipherException if an error occurs during key generation.
     * @throws SocketException if an I/O error occurs during the exchange.
     */
    public ClientProxy(String serverHost, int serverPort) throws CipherException, SocketException {
        this.serverHost = serverHost;
        this.serverPort = serverPort;

        DHKeyExchange dhKeyExchange = new DHKeyExchange();
        dhKeyExchange.performsKeyExchange(serverHost, serverPort);
        this.secretKey = dhKeyExchange.getSecretKey();
    }

    /**
     * Starts the proxy server in the client side.
     *
     * @param port The port number for the proxy server.
     *
     * @throws SocketException if a socket error occurs.
     */
    public void startServer(int port) throws SocketException, CipherException {
        try {
            ServerSocket proxyServer = new ServerSocket(port);
            proxyServer.setReuseAddress(true);
            logger.log("[ClientProxy] Started on localhost, port " + port);

            ConnectionCipher cipher = new ConnectionCipher();
            cipher.setSecretKey(new SecretKeySpec(this.secretKey, 0, 16, "AES"));

            while (true) {
                logger.log("[ClientProxy] Waiting for a new connection...");
                Socket proxySocket = proxyServer.accept();

                ClientProxyConnection connection = new ClientProxyConnection(
                        proxySocket,
                        cipher,
                        this.serverHost,
                        this.serverPort
                );
                connection.start();
            }

        } catch (IOException e) {
            throw new SocketException(e.getMessage(), e);
        }
    }

    /**
     * ClientProxy application entry-point.
     *
     * @param args Console arguments.
     */
    public static void main(String[] args) {

        try {
            ClientProxy client = new ClientProxy("localhost", 4321);
            client.startServer(1234);

        } catch (Throwable e) {
            Logger.getInstance().error("FATAL ERROR", e);
        }
    }

}
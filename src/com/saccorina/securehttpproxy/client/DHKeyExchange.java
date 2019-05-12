package com.saccorina.securehttpproxy.client;

import com.saccorina.securehttpproxy.Logger;
import com.saccorina.securehttpproxy.Utility;
import com.saccorina.securehttpproxy.exception.CipherException;
import com.saccorina.securehttpproxy.exception.SocketException;

import javax.crypto.KeyAgreement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * This class performs a Diffie-Hellman Key Exchange from the client side.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
class DHKeyExchange {

    private static final Logger logger = Logger.getInstance();

    private KeyAgreement keyAgreement;

    private byte[] secretKey;

    /**
     * Create a key pair for this entity.
     *
     * @return Returns the key pair for this entity.
     *
     * @throws CipherException if an error occurs.
     */
    private KeyPair createKeyPair() throws CipherException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            this.keyAgreement = KeyAgreement.getInstance("DH");
            this.keyAgreement.init(keyPair.getPrivate());

            logger.log("[DHKeyExchange] Key pair created");
            return keyPair;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new CipherException("Error during key pair creation: " + e.getMessage(), e);
        }
    }

    /**
     * Performs the Diffie-Hellman Key Exchange.
     *
     * @param host The host of the secondary entity.
     * @param port The port of the secondary entity.
     *
     * @throws CipherException if an error occurs during key generation.
     * @throws SocketException if an I/O error occurs during the exchange.
     */
    void performsKeyExchange(String host, int port) throws CipherException, SocketException {
        try {
            // create a key pair (private/public)
            KeyPair keyPair = this.createKeyPair();
            byte[] publicKey = keyPair.getPublic().getEncoded();

            // connect to the other entity
            logger.log("[DHKeyExchange] Connecting to the server...");
            Socket socket = new Socket(host, port);

            // send public key
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(Utility.bytesToHexString(publicKey));
            out.println();
            logger.log("[DHKeyExchange] Personal public key sent");

            // receive other public key
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder messageBuilder = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.isEmpty()) break;
                messageBuilder.append(inputLine);
            }
            byte[] otherPublicKeyEnc = Utility.hexStringToBytes(messageBuilder.toString());
            // decode other public key
            KeyFactory keyFactory = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(otherPublicKeyEnc);
            PublicKey otherPublicKey = keyFactory.generatePublic(x509KeySpec);
            logger.log("[DHKeyExchange] Other public key received");

            // the DH key agreement protocol is completed
            this.keyAgreement.doPhase(otherPublicKey, true);

            // generate the shared secret
            this.secretKey = this.keyAgreement.generateSecret();
            logger.log("[DHKeyExchange] Secret key generated");

        } catch (IOException e) {
            throw new SocketException(e.getMessage(), e);

        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
            throw new CipherException(e.getMessage(), e);
        }
    }

    /**
     * Get the generated shared secret key.
     *
     * @return Returns the shared secret key.
     */
    byte[] getSecretKey() {
        return this.secretKey;
    }

}

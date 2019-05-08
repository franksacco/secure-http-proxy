package com.saccorina.securehttpproxy;

import com.saccorina.securehttpproxy.exception.CipherException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

/**
 * Performs encryption and decryption of messages.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
public class ConnectionCipher {

    private Cipher cipher;

    private IvParameterSpec initializationVector;

    /**
     * Initialize the connection cipher.
     */
    public ConnectionCipher() throws CipherException {
        this("AES", "ECB", "PKCS5Padding");
    }

    /**
     * Initialize the connection cipher.
     *
     * @param algorithm Algorithm name (e.g. AES).
     * @param mode Encryption/decryption mode (ECB, CBC, OFB, CFB, CTR).
     * @param padding Padding mode (e.g. PKCS5Padding, NoPadding).
     *
     * @throws CipherException for an empty algorithm or padding mode.
     */
    public ConnectionCipher(String algorithm, String mode, String padding) throws CipherException {
        try {
            this.cipher = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new CipherException(e.getMessage(), e);
        }
    }

    /**
     * Encrypt a message.
     *
     * @param key The secret key in bytes used for encryption.
     * @param message The message in bytes to be encrypted.
     * @return Returns the result in bytes of the encryption.
     *
     * @throws CipherException for an invalid value.
     */
    public byte[] encrypt(byte[] key, byte[] message) throws CipherException {

        try {
            SecretKey secret = new SecretKeySpec(key, cipher.getAlgorithm().split("/")[0]);

            this.cipher.init(Cipher.ENCRYPT_MODE, secret, this.initializationVector);
            return this.cipher.doFinal(message);

        } catch (GeneralSecurityException e) {
            throw new CipherException(e.getMessage(), e);
        }
    }

    /**
     * Decrypt a message.
     *
     * @param key The secret key in bytes used for decryption.
     * @param message The message in bytes to be decrypted.
     * @return Returns the result in bytes of the decryption.
     *
     * @throws CipherException for an invalid value.
     */
    public byte[] decrypt(byte[] key, byte[] message) throws CipherException {
        try {
            SecretKey secret = new SecretKeySpec(key, cipher.getAlgorithm().split("/")[0]);

            this.cipher.init(Cipher.DECRYPT_MODE, secret, this.initializationVector);
            return this.cipher.doFinal(message);

        } catch (GeneralSecurityException e) {
            throw new CipherException(e.getMessage(), e);
        }
    }

}

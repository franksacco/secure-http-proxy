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

    /**
     * The cipher instance.
     */
    private Cipher cipher;

    /**
     * The initialization vector used in CBC, OFB and CFB modes.
     */
    private IvParameterSpec initializationVector;

    /**
     * The secret key used for encryption/decryption.
     */
    private SecretKey secretKey;

    /**
     * Initialize the connection cipher with AES/ECB/PKCS5Padding.
     *
     * @throws CipherException for an empty algorithm or padding mode.
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
     * Set the initialization vector used in CBC, OFB and CFB modes.
     *
     * @param iv The initialization vector in bytes.
     */
    public void setInitializationVector(byte[] iv) {
        this.initializationVector = new IvParameterSpec(iv);
    }

    /**
     * Set the secret key used for encryption/decryption.
     *
     * @param secretKey The secret key specifications.
     */
    public void setSecretKey(SecretKeySpec secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Encrypt a message.
     *
     * @param message The message in bytes to be encrypted.
     * @return Returns the result in bytes of the encryption.
     *
     * @throws CipherException for an invalid value.
     */
    public byte[] encrypt(byte[] message) throws CipherException {

        if (this.secretKey == null) {
            throw new CipherException("The secret key must be set before encryption");
        }

        try {
            this.cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, this.initializationVector);
            return this.cipher.doFinal(message);

        } catch (GeneralSecurityException e) {
            throw new CipherException(e.getMessage(), e);
        }
    }

    /**
     * Decrypt a message.
     *
     * @param message The message in bytes to be decrypted.
     * @return Returns the result in bytes of the decryption.
     *
     * @throws CipherException for an invalid value.
     */
    public byte[] decrypt(byte[] message) throws CipherException {

        if (this.secretKey == null) {
            throw new CipherException("The secret key must be set before decryption");
        }

        try {
            this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey, this.initializationVector);
            return this.cipher.doFinal(message);

        } catch (GeneralSecurityException e) {
            throw new CipherException(e.getMessage(), e);
        }
    }

}

package com.saccorina.securehttpproxy.exception;

/**
 * Exception thrown during the encryption or decryption of messages.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
public class CipherException extends Exception {

    /**
     * {@inheritDoc}
     */
    public CipherException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public CipherException(String message, Throwable e) {
        super(message, e);
    }

}

package com.saccorina.securehttpproxy.exception;

/**
 * Exception thrown when an error occur using sockets.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
public class SocketException extends Exception {

    /**
     * {@inheritDoc}
     */
    public SocketException(String message, Throwable cause) {
        super(message, cause);
    }

}

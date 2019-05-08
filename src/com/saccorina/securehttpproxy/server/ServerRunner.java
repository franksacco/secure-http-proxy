package com.saccorina.securehttpproxy.server;

/**
 * This class provides the entry-point for the application in the server-side.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
public class ServerRunner {

    /**
     * Server-side application entry-point.
     *
     * @param args Console arguments.
     */
    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.startServer();

        } catch (Throwable e) {
            System.out.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

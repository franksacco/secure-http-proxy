package com.saccorina.securehttpproxy.client;

/**
 * This class provides the entry-point for the application in the client-side.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
public class ClientRunner {

    /**
     * Client-side application entry-point.
     *
     * @param args Console arguments.
     */
    public static void main(String[] args) {

        try {
            Client client = new Client();
            client.startProxyServer();

        } catch (Throwable e) {
            System.out.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

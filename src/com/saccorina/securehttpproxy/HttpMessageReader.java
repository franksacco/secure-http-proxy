package com.saccorina.securehttpproxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for transforming HTTP message from input stream to byte array.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
public class HttpMessageReader {

    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HEADER_HOST = "Host";

    /**
     * The starting header that defines a request or a response.
     */
    private byte[] startingHeader;

    /**
     * The hash map of HTTP headers.
     */
    private Map<String, ArrayList<String>> headers;

    /**
     * The message payload in bytes.
     */
    private byte[] payload;

    /**
     * Initialize message reader and parse HTTP headers.
     *
     * @param in The input stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    public HttpMessageReader(InputStream in) throws IOException {
        this.startingHeader = this.parseStartingHeader(in);
        this.headers = this.parseHeaders(in);
        this.payload = this.parsePayload(in);
    }

    /**
     * Read an array of bytes until an EOL ("\n") is reached.
     *
     * @param in The input stream to read.
     * @return Returns a line as an array of bytes or NULL if no bytes are available.
     *
     * @throws IOException if an I/O error occurs.
     */
    private byte[] readLine(InputStream in) throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();

        int b;
        while ((b = in.read()) != -1) {
            if ((char) b == '\n') break;
            line.write(b);
        }

        return line.size() == 0 ? null : line.toByteArray();
    }

    /**
     * Retrieves the starting header of the HTTP message.
     *
     * @param in The input stream.
     * @return Returns the starting HTTP header in bytes.
     *
     * @throws IOException if an I/O error occurs.
     */
    private byte[] parseStartingHeader(InputStream in) throws IOException {
        byte[] line = this.readLine(in);
        if (line == null) {
            throw new IOException("No starting header found in message");
        }
        return new String(line).trim().concat("\r\n").getBytes();
    }

    /**
     * Parses the HTTP headers and retrieves an hash map with all values.
     *startingHeader.length
     * @param in The input stream to read.
     * @return Returns the hash map that contains all HTTP headers.
     *
     * @throws IOException if an I/O error occurs.
     */
    private Map<String, ArrayList<String>> parseHeaders(InputStream in) throws IOException {
        Map<String, ArrayList<String>> headers = new HashMap<>();

        byte[] line;
        String[] header;
        ArrayList<String> headerValues;
        while ((line = this.readLine(in)) != null) {
            if (line.length <= 1) break; // empty line between headers and payload

            header = new String(line).split(":", 2);
            if (header.length < 2) continue; // skip malformed headers

            headerValues = headers.getOrDefault(header[0], new ArrayList<>());
            headerValues.add(header[1].trim());
            headers.put(header[0].trim(), headerValues);
        }

        return headers;
    }

    /**
     * Retrieves the payload of the HTTP message.
     *
     * @param in The input stream to read.
     * @return Returns the message payload
     *
     * @throws IOException if an I/O error occurs.
     */
    private byte[] parsePayload(InputStream in) throws IOException {
        ArrayList<String> contentLengthHeader = headers.get(HEADER_CONTENT_LENGTH);
        if (contentLengthHeader != null) {

            int contentLength = Integer.parseInt(contentLengthHeader.get(0));
            if (contentLength > 0) {
                byte[] payload = new byte[contentLength];
                in.read(payload, 0, contentLength);
                return payload;
            }
        }
        return null;
    }

    /**
     * Retrieves the host of the HTTP request.
     *
     * @return Returns the host or NULL if the header is not present.
     */
    public String getHost() {
        ArrayList<String> hostHeader = headers.get(HEADER_HOST);
        if (hostHeader == null) {
            return null;
        }
        return hostHeader.get(0).split(":")[0]; // remove port if present
    }

    /**
     * Retrieves the entire HTTP message as an array of bytes.
     *
     * @return Returns the message as a byte array.
     */
    public byte[] getMessage() {
        StringBuilder headersBuilder = new StringBuilder();
        this.headers.forEach((name, values) -> {
            for (String v : values) {
                if (name != null) {
                    headersBuilder.append(name).append(": ");
                }
                headersBuilder.append(v).append("\r\n");
            }
        });
        headersBuilder.append("\r\n");
        byte[] headers = headersBuilder.toString().getBytes();

        byte[] message = new byte[startingHeader.length + headers.length + (payload == null ? 0 : payload.length)];
        System.arraycopy(startingHeader, 0, message, 0, startingHeader.length);
        System.arraycopy(headers, 0, message, startingHeader.length, headers.length);
        if (payload != null) {
            System.arraycopy(payload, 0, message, startingHeader.length + headers.length, payload.length);
        }
        return message;
    }

}

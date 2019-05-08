package com.saccorina.securehttpproxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Project utilities.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
public class Utility {

    /**
     *
     * @param buf
     * @return
     */
    public static String bytesToHexString(byte[] buf) {
        StringBuilder builder = new StringBuilder();
        for (byte b : buf) {
            builder.append(Integer.toHexString((b >> 4) & 0x0f))
                    .append(Integer.toHexString(b & 0x0f));
        }
        return builder.toString();
    }

    /**
     *
     * @param str
     * @return
     */
    public static byte[] hexStringToBytes(String str) {
        byte[] buffer = new byte[str.length()/2];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) Integer.parseInt(str.substring(i*2, i*2 + 2),16);
        }
        return buffer;
    }

    /**
     * Converts an input stream to a byte array.
     *
     * @param in The input stream.
     * @return Returns the byte array.
     *
     * @throws IOException if the first byte cannot be read for any reason
     *     other than the end of the file, if the input stream has been
     *     closed, or if some other I/O error occurs.
     */
    public static byte[] inputStreamToByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }
}

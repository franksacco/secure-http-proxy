package com.saccorina.securehttpproxy;

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

}

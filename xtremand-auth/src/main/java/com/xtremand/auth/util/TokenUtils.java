package com.xtremand.auth.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class TokenUtils {

    public static String decodeHexToken(String rawToken) {
        if (rawToken.startsWith("\\x")) {
            rawToken = rawToken.substring(2); // Remove \x
        }
        try {
            byte[] decoded = Hex.decodeHex(rawToken.toCharArray()); // ✅ FIXED
            return new String(decoded);
        } catch (DecoderException e) {
            throw new TokenDecodeException("Failed to decode hex token", e);
        }
    }
}

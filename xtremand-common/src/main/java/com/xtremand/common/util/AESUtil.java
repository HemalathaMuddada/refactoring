package com.xtremand.common.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class AESUtil {

	private static final int AES_KEY_SIZE = 128; // in bits
    private static final int IV_SIZE = 12; // 96 bits is standard for GCM
    private static final int TAG_BIT_LENGTH = 128;
    private static final String AES_KEY_STRING = "changeit12345678"; // 16 bytes (128-bit)

    // Generate key from fixed string
    private static SecretKey generateKey() {
        return new SecretKeySpec(AES_KEY_STRING.getBytes(StandardCharsets.UTF_8), "AES");
    }

    // Encrypt
    public static String encrypt(String plainText) {
        SecretKey key;
        try {
            key = generateKey();
            byte[] iv = new byte[12]; // 12 bytes IV for GCM
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv); // generate secure random IV

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);
            byte[] combined = byteBuffer.array();

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // Decrypt
    public static String decrypt(String base64CipherText) {
        SecretKey key;
        try {
            key = generateKey();
            byte[] decoded = Base64.getDecoder().decode(base64CipherText);
            byte[] iv = Arrays.copyOfRange(decoded, 0, 12); // first 12 bytes = IV
            byte[] cipherBytes = Arrays.copyOfRange(decoded, 12, decoded.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            byte[] decrypted = cipher.doFinal(cipherBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
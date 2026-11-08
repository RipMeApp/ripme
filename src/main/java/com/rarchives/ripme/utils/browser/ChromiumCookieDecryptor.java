package com.rarchives.ripme.utils.browser;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Decrypts the {@code encrypted_value} column of Chromium-family cookie databases.
 * <ul>
 * <li>Windows: {@code v10} + 12-byte nonce + AES-256-GCM ciphertext/tag (key from Local State via DPAPI).</li>
 * <li>macOS / Linux: {@code v10} (or {@code v11}) + AES-128-CBC ciphertext with an all-space IV.</li>
 * </ul>
 * Values with any other prefix, notably {@code v20} (app-bound encryption, Chrome/Edge 127+ on
 * Windows), cannot be decrypted from outside the browser and are reported as unsupported.
 */
final class ChromiumCookieDecryptor {

    static final class UnsupportedEncryptionException extends Exception {
        private static final long serialVersionUID = 1L;

        UnsupportedEncryptionException(String message) {
            super(message);
        }
    }

    private static final int PREFIX_LENGTH = 3;
    private static final int GCM_NONCE_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int HOST_HASH_LENGTH = 32;

    private ChromiumCookieDecryptor() {
    }

    /** Derives the 16-byte AES key Chromium uses on macOS (1003 iterations) and Linux (1 iteration). */
    static byte[] derivePosixKey(String password, int iterations) throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), "saltysalt".getBytes(StandardCharsets.US_ASCII),
                iterations, 128);
        return factory.generateSecret(spec).getEncoded();
    }

    /**
     * @param encrypted    raw {@code encrypted_value} bytes
     * @param keysByPrefix AES key for each supported version prefix, e.g. {@code "v10"}
     * @param hostKey      the cookie's domain column, used to strip the integrity hash newer browsers prepend
     * @param gcm          true for the Windows scheme (AES-GCM), false for the macOS/Linux scheme (AES-CBC)
     */
    static String decrypt(byte[] encrypted, Map<String, byte[]> keysByPrefix, String hostKey, boolean gcm)
            throws GeneralSecurityException, UnsupportedEncryptionException {
        if (encrypted.length < PREFIX_LENGTH) {
            throw new UnsupportedEncryptionException("value too short");
        }
        String prefix = new String(encrypted, 0, PREFIX_LENGTH, StandardCharsets.US_ASCII);
        byte[] key = keysByPrefix.get(prefix);
        if (key == null) {
            throw new UnsupportedEncryptionException("unsupported encryption version " + prefix);
        }

        byte[] plain;
        if (gcm) {
            if (encrypted.length < PREFIX_LENGTH + GCM_NONCE_LENGTH + GCM_TAG_LENGTH) {
                throw new UnsupportedEncryptionException("truncated value");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH * 8, encrypted, PREFIX_LENGTH, GCM_NONCE_LENGTH));
            int offset = PREFIX_LENGTH + GCM_NONCE_LENGTH;
            plain = cipher.doFinal(encrypted, offset, encrypted.length - offset);
        } else {
            byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) ' ');
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            plain = cipher.doFinal(encrypted, PREFIX_LENGTH, encrypted.length - PREFIX_LENGTH);
        }

        // Browsers with cookie DB version >= 24 put SHA-256(host_key) in front of the value.
        if (plain.length >= HOST_HASH_LENGTH) {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(hostKey.getBytes(StandardCharsets.UTF_8));
            if (Arrays.equals(Arrays.copyOf(plain, HOST_HASH_LENGTH), hash)) {
                plain = Arrays.copyOfRange(plain, HOST_HASH_LENGTH, plain.length);
            }
        }
        return new String(plain, StandardCharsets.UTF_8);
    }
}

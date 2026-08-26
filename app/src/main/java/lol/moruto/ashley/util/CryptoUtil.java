package lol.moruto.ashley.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    private CryptoUtil() {}

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) hex.append(String.format("%02x", b));

            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String generatePassword(int length, boolean uppercase, boolean lowercase, boolean numbers, boolean symbols) {

        if (length <= 0) throw new IllegalArgumentException("Length must be greater than 0.");

        StringBuilder chars = new StringBuilder();

        if (uppercase) chars.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (lowercase) chars.append("abcdefghijklmnopqrstuvwxyz");
        if (numbers) chars.append("0123456789");
        if (symbols) chars.append("!@#$%^&*()-_=+[]{}<>?/|");
        if (chars.length() == 0) throw new IllegalArgumentException("Select at least one character set.");

        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }

        return password.toString();
    }

    public static byte[] encrypt(byte[] bytes, String password) throws Exception {
        if (bytes == null) throw new IllegalArgumentException("Data cannot be null");
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Password cannot be empty");

        byte[] iv = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        SecretKey key = getKey(password);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));

        byte[] encrypted = cipher.doFinal(bytes);

        byte[] result = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, result, 0, iv.length);

        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

        return result;
    }

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private static String getFileName(Context context, Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            } catch (Exception ignored) {}
        }

        if (result == null) result = uri.getLastPathSegment();

        return result;
    }

    public static Uri encryptFile(Context context, Uri inputUri, String password) throws Exception {

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        File dir = new File(
                context.getExternalFilesDir(null),
                "encrypted"
        );

        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create output directory");
        }

        String inputName = getFileName(context, inputUri);

        if (inputName == null) {
            inputName = "encrypted_file";
        }

        File outputFile = new File(
                dir,
                inputName + ".enc"
        );

        byte[] iv = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        SecretKey key = getKey(password);

        Cipher cipher = Cipher.getInstance(
                "AES/GCM/NoPadding"
        );

        cipher.init(
                Cipher.ENCRYPT_MODE,
                key,
                new GCMParameterSpec(128, iv)
        );

        try (
                InputStream input =
                        context.getContentResolver()
                                .openInputStream(inputUri);

                FileOutputStream fileOutput =
                        new FileOutputStream(outputFile)
        ) {
            if (input == null) {
                throw new IOException("Could not open input file");
            }

            fileOutput.write(iv);

            try (CipherOutputStream cipherOutput =
                         new CipherOutputStream(fileOutput, cipher)) {

                byte[] buffer = new byte[8192];
                int read;

                while ((read = input.read(buffer)) != -1) {
                    cipherOutput.write(buffer, 0, read);
                }
            }
        }

        return Uri.fromFile(outputFile);
    }

    public static String hashFile(android.content.Context context, android.net.Uri uri, String algorithm) throws Exception {

        java.security.MessageDigest digest =
                java.security.MessageDigest.getInstance(algorithm);

        try (
                java.io.InputStream in =
                        context.getContentResolver().openInputStream(uri)
        ) {

            if (in == null) {
                throw new java.io.IOException(
                        "Unable to open file"
                );
            }

            byte[] buffer = new byte[8192];

            int read;

            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        return bytesToHex(digest.digest());
    }


    public static String bytesToHex(byte[] bytes) {

        char[] hex = "0123456789abcdef".toCharArray();

        char[] result = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {

            int value = bytes[i] & 0xff;

            result[i * 2] =
                    hex[value >>> 4];

            result[i * 2 + 1] =
                    hex[value & 0x0f];
        }

        return new String(result);
    }


    public static byte[] hexToBytes(String hex) {

        hex = hex.trim();

        if ((hex.length() & 1) != 0) {
            throw new IllegalArgumentException(
                    "Hex string must have an even length"
            );
        }

        byte[] result = new byte[hex.length() / 2];

        for (int i = 0; i < hex.length(); i += 2) {

            int high = Character.digit(
                    hex.charAt(i),
                    16
            );

            int low = Character.digit(
                    hex.charAt(i + 1),
                    16
            );

            if (high == -1 || low == -1) {
                throw new IllegalArgumentException(
                        "Invalid hexadecimal character"
                );
            }

            result[i / 2] =
                    (byte) ((high << 4) | low);
        }

        return result;
    }

    public static byte[] decrypt(byte[] bytes, String password) {
        try {
            if (bytes.length < IV_LENGTH + 16) {
                throw new IllegalArgumentException(
                        "Invalid encrypted data"
                );
            }

            byte[] iv = new byte[IV_LENGTH];

            System.arraycopy(bytes, 0, iv, 0, IV_LENGTH);

            byte[] encrypted = new byte[bytes.length - IV_LENGTH];

            System.arraycopy(bytes, IV_LENGTH, encrypted, 0, encrypted.length);

            SecretKey key = getKey(password);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));

            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private static SecretKey getKey(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            return new SecretKeySpec(digest.digest(password.getBytes(StandardCharsets.UTF_8)), "AES");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifySha256(String text, String expectedHash) {
        return sha256(text).equalsIgnoreCase(expectedHash.trim());
    }

    public static String base64Encode(String input) {
        return Base64.encodeToString(input.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }

    public static String base64Decode(String input) {
        try {
            return new String(Base64.decode(input, Base64.DEFAULT), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Base64.");
        }
    }
}

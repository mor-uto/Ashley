package lol.moruto.ashley.util;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class ImageCrypto {

    private static final String ALIAS = "AshleyImageKey";
    private static final String STORE = "AndroidKeyStore";

    private static SecretKey getKey() throws Exception {

        KeyStore ks = KeyStore.getInstance(STORE);
        ks.load(null);

        if (ks.containsAlias(ALIAS))
            return ((KeyStore.SecretKeyEntry)
                    ks.getEntry(ALIAS, null)).getSecretKey();

        KeyGenerator generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                STORE
        );

        generator.init(
                new KeyGenParameterSpec.Builder(
                        ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT
                )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(
                                KeyProperties.ENCRYPTION_PADDING_NONE
                        )
                        .build()
        );

        return generator.generateKey();
    }

    public static byte[] encrypt(byte[] data) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.ENCRYPT_MODE,
                getKey()
        );

        byte[] encrypted = cipher.doFinal(data);
        byte[] iv = cipher.getIV();

        ByteBuffer buffer = ByteBuffer.allocate(
                4 + iv.length + encrypted.length
        );

        buffer.putInt(iv.length);
        buffer.put(iv);
        buffer.put(encrypted);

        return buffer.array();
    }

    public static byte[] decrypt(byte[] data) throws Exception {

        ByteBuffer buffer = ByteBuffer.wrap(data);

        int ivLength = buffer.getInt();

        byte[] iv = new byte[ivLength];
        buffer.get(iv);

        byte[] encrypted = new byte[buffer.remaining()];
        buffer.get(encrypted);

        Cipher cipher = Cipher.getInstance(
                "AES/GCM/NoPadding"
        );

        cipher.init(
                Cipher.DECRYPT_MODE,
                getKey(),
                new GCMParameterSpec(128, iv)
        );

        return cipher.doFinal(encrypted);
    }
}
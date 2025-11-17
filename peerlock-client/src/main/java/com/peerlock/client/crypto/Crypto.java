package com.peerlock.client.crypto;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

    public static final String EC_CURVE = "secp256r1"; // widely supported
    public static final int GCM_TAG_LENGTH = 128;      // bits
    public static final int GCM_NONCE_LENGTH = 12;     // bytes

    private static final SecureRandom RANDOM = new SecureRandom();

    public static KeyPair generateEcKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(new ECGenParameterSpec(EC_CURVE));
            return kpg.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate EC key pair", e);
        }
    }

    public static SecretKey deriveSharedKey(PrivateKey privateKey, PublicKey remotePublicKey) {
        try {
            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(privateKey);
            ka.doPhase(remotePublicKey, true);
            byte[] sharedSecret = ka.generateSecret();

            // Derive AES key from shared secret via SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(sharedSecret);
            return new SecretKeySpec(keyBytes, 0, 16, "AES"); // 128-bit AES
        } catch (Exception e) {
            throw new RuntimeException("Failed to derive shared key", e);
        }
    }

    public static String encodePublicKey(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static PublicKey decodePublicKey(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode public key", e);
        }
    }

    public static EncryptedPayload encrypt(SecretKey key, byte[] plaintext) {
        try {
            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            RANDOM.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] ciphertext = cipher.doFinal(plaintext);

            return new EncryptedPayload(
                    Base64.getEncoder().encodeToString(nonce),
                    Base64.getEncoder().encodeToString(ciphertext)
            );
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static byte[] decrypt(SecretKey key, String base64Nonce, String base64Ciphertext) {
        try {
            byte[] nonce = Base64.getDecoder().decode(base64Nonce);
            byte[] ciphertext = Base64.getDecoder().decode(base64Ciphertext);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public record EncryptedPayload(String nonceBase64, String ciphertextBase64) { }
}

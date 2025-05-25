package com.korgun.bankservice.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CardCryptoUtils {

    private static final String SECRET = "1234567890123456";

    public static String encrypt(String cardNumber) {
        try {
            if (cardNumber == null || cardNumber.isEmpty()) {
                throw new IllegalArgumentException("Card number cannot be null or empty");
            }

            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting card number", e);
        }
    }

    public static String decrypt(String encryptedCardNumber) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKey = new SecretKeySpec(SECRET.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber));
        return new String(decryptedBytes);
    }

    public static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}

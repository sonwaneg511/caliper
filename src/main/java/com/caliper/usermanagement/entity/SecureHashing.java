package com.caliper.usermanagement.entity;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureHashing {
	private static final SecureRandom secureRandom = new SecureRandom(); // Thread safe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public static String generateToken() {
        byte[] randomBytes = new byte[32]; // 32 bytes = 256-bit token
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
    
    public static String encryptPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}

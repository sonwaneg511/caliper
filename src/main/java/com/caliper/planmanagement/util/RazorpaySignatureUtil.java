package com.caliper.planmanagement.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class RazorpaySignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public static boolean verifyPaymentSignature(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature,
            String keySecret) throws NoSuchAlgorithmException, InvalidKeyException {

        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        String computed = computeHmac(payload, keySecret);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                razorpaySignature.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean verifyWebhookSignature(
            String rawBody,
            String receivedSignature,
            String webhookSecret) throws NoSuchAlgorithmException, InvalidKeyException {

        String computed = computeHmac(rawBody, webhookSecret);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8));
    }

    private static String computeHmac(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

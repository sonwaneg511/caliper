package com.caliper.razorpay.controller;

import com.caliper.razorpay.service.RazorpaySubscriptionWebhookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/razorpay")
@Tag(name = "Razorpay Subscription Webhook", description = "Razorpay subscription event receiver")
public class RazorpayWebhookController {

    private static final Logger log = LoggerFactory.getLogger(RazorpayWebhookController.class);

    @Autowired
    private RazorpaySubscriptionWebhookService webhookService;

    /**
     * Receives Razorpay subscription lifecycle events (subscription.activated,
     * subscription.charged, subscription.halted, subscription.cancelled, payment.failed).
     * Register this URL in Razorpay Dashboard → Webhooks.
     * Secured via HMAC-SHA256 signature — no JWT required.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        if (signature == null || signature.isBlank()) {
            log.warn("Razorpay subscription webhook received without signature header");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing signature");
        }

        if (!webhookService.verifySignature(payload, signature)) {
            log.warn("Razorpay subscription webhook signature verification failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        webhookService.processEvent(payload);

        return ResponseEntity.ok("OK");
    }
}

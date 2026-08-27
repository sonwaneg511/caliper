package com.caliper.planmanagement.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.caliper.planmanagement.entity.payment.RazorpayOrder;
import com.caliper.planmanagement.repository.payment.RazorpayOrderRepository;
import com.caliper.planmanagement.util.RazorpaySignatureUtil;

@Service
public class RazorpayWebhookService {

    private static final Logger log = LoggerFactory.getLogger(RazorpayWebhookService.class);

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Autowired
    private RazorpayOrderRepository razorpayOrderRepository;

    @Autowired
    private PlanActivationService planActivationService;

    public ResponseEntity<String> handleWebhook(String rawBody, String signatureHeader) {
        // Step 1: Verify webhook signature
        try {
            boolean isValid = RazorpaySignatureUtil.verifyWebhookSignature(rawBody, signatureHeader, webhookSecret);
            if (!isValid) {
                log.warn("Invalid Razorpay webhook signature");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Webhook signature verification error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature verification error");
        }

        // Step 2: Parse event
        JSONObject payload;
        try {
            payload = new JSONObject(rawBody);
        } catch (Exception e) {
            log.error("Failed to parse webhook body: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON payload");
        }

        String event = payload.optString("event", "");
        String eventId = payload.optString("id", null);
        log.info("Received Razorpay webhook event: {}", event);

        if (!event.startsWith("payment.")) {
            return ResponseEntity.ok("event_ignored");
        }

        JSONObject paymentEntity;
        try {
            paymentEntity = payload
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");
        } catch (Exception e) {
            log.error("Could not extract payment entity from webhook: {}", e.getMessage());
            return ResponseEntity.ok("payload_parse_error");
        }

        String razorpayOrderId = paymentEntity.optString("order_id", null);
        String razorpayPaymentId = paymentEntity.optString("id", null);

        if (razorpayOrderId == null) {
            return ResponseEntity.ok("no_order_id");
        }

        Optional<RazorpayOrder> orderOpt = razorpayOrderRepository.findByRazorpayOrderId(razorpayOrderId);
        if (orderOpt.isEmpty()) {
            log.warn("Webhook received for unknown orderId={}", razorpayOrderId);
            return ResponseEntity.ok("order_not_found");
        }

        RazorpayOrder order = orderOpt.get();

        // Idempotency: already PAID
        if (RazorpayOrder.STATUS_PAID.equals(order.getStatus())) {
            log.info("Webhook for already-paid order {}, ignoring", razorpayOrderId);
            return ResponseEntity.ok("already_processed");
        }

        if ("payment.captured".equals(event)) {
            try {
                planActivationService.activatePlan(order, razorpayPaymentId, "webhook");
                log.info("Plan activated via webhook for orderId={}", razorpayOrderId);
            } catch (DataIntegrityViolationException e) {
                log.warn("Duplicate webhook event={} for orderId={}, ignoring", eventId, razorpayOrderId);
                return ResponseEntity.ok("already_processed");
            } catch (Exception e) {
                log.error("Plan activation failed for orderId={}: {}", razorpayOrderId, e.getMessage());
                // Return 200 to prevent Razorpay retries flooding; investigate via logs
                return ResponseEntity.ok("activation_error");
            }
        } else if ("payment.failed".equals(event)) {
            order.setStatus(RazorpayOrder.STATUS_FAILED);
            razorpayOrderRepository.save(order);
            log.info("Order {} marked FAILED via webhook", razorpayOrderId);
        }

        return ResponseEntity.ok("success");
    }
}

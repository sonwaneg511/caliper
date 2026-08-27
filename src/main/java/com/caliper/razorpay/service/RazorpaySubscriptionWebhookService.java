package com.caliper.razorpay.service;

import com.caliper.location.entity.Client;
import com.caliper.location.repository.ClientRepository;
import com.caliper.planmanagement.dto.GstBreakdownDto;
import com.caliper.planmanagement.entity.Plan;
import com.caliper.planmanagement.entity.payment.CaliperPayment;
import com.caliper.planmanagement.repository.PlanRepository;
import com.caliper.planmanagement.repository.payment.CaliperPaymentRepository;
import com.caliper.planmanagement.service.PricingService;
import com.caliper.planmanagement.util.RazorpaySignatureUtil;
import com.caliper.razorpay.entity.RazorpaySubscription;
import com.caliper.razorpay.repository.RazorpaySubscriptionRepository;
import com.caliper.utils.email.EmailService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class RazorpaySubscriptionWebhookService {

    private static final Logger log = LoggerFactory.getLogger(RazorpaySubscriptionWebhookService.class);

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Autowired
    private RazorpaySubscriptionRepository subscriptionRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private CaliperPaymentRepository caliperPaymentRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PricingService pricingService;

    // -------------------------------------------------------------------
    // SIGNATURE VERIFICATION — delegates to shared utility
    // -------------------------------------------------------------------
    public boolean verifySignature(String payload, String razorpaySignature) {
        try {
            return RazorpaySignatureUtil.verifyWebhookSignature(payload, razorpaySignature, webhookSecret);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Subscription webhook signature verification error: {}", e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------
    // DISPATCH WEBHOOK EVENT
    // -------------------------------------------------------------------
    public void processEvent(String payload) {
        try {
            JSONObject event = new JSONObject(payload);
            String eventType = event.optString("event", "");
            log.info("Processing Razorpay subscription webhook event: {}", eventType);

            switch (eventType) {
                case "subscription.activated"   -> handleSubscriptionActivated(event);
                case "subscription.charged"     -> handleSubscriptionCharged(event);
                case "subscription.halted"      -> handleSubscriptionHalted(event);
                case "subscription.cancelled"   -> handleSubscriptionCancelled(event);
                case "payment.failed"           -> handlePaymentFailed(event);
                default -> log.debug("Unhandled subscription event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing Razorpay subscription webhook: {}", e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------
    // subscription.activated — customer completed mandate registration
    // -------------------------------------------------------------------
    private void handleSubscriptionActivated(JSONObject event) {
        JSONObject subEntity = event.getJSONObject("payload").getJSONObject("subscription").getJSONObject("entity");
        String rzpSubId = subEntity.getString("id");

        subscriptionRepository.findByRazorpaySubscriptionId(rzpSubId).ifPresent(sub -> {
            sub.setStatus(RazorpaySubscription.STATUS_ACTIVE);
            sub.setUpdatedAt(new Date());
            subscriptionRepository.save(sub);

            activateCaliperPlan(sub);

            log.info("Subscription {} activated for client {}", rzpSubId, sub.getClientId());
        });
    }

    // -------------------------------------------------------------------
    // subscription.charged — automatic renewal payment succeeded
    // -------------------------------------------------------------------
    private void handleSubscriptionCharged(JSONObject event) {
        String eventId = event.optString("id", null);
        JSONObject payload = event.getJSONObject("payload");
        JSONObject subEntity = payload.getJSONObject("subscription").getJSONObject("entity");
        JSONObject paymentEntity = payload.has("payment")
                ? payload.getJSONObject("payment").getJSONObject("entity")
                : new JSONObject();

        String rzpSubId = subEntity.getString("id");

        subscriptionRepository.findByRazorpaySubscriptionId(rzpSubId).ifPresent(sub -> {
            // Update subscription state
            sub.setPaidCount(subEntity.optInt("paid_count", sub.getPaidCount() != null ? sub.getPaidCount() + 1 : 1));
            if (subEntity.has("current_end") && !subEntity.isNull("current_end"))
                sub.setCurrentEnd(new Date(subEntity.getLong("current_end") * 1000L));
            if (subEntity.has("charge_at") && !subEntity.isNull("charge_at"))
                sub.setNextChargeAt(new Date(subEntity.getLong("charge_at") * 1000L));
            sub.setStatus(RazorpaySubscription.STATUS_ACTIVE);
            sub.setUpdatedAt(new Date());
            subscriptionRepository.save(sub);

            // Compute amount and GST
            long amountPaise = paymentEntity.has("amount")
                    ? paymentEntity.getLong("amount")
                    : Math.round(sub.getAmount() * 100);

            GstBreakdownDto gst = pricingService.computeGst(amountPaise);
            BigDecimal cgstRupees = BigDecimal.valueOf(gst.getCgstPaise()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal sgstRupees = BigDecimal.valueOf(gst.getSgstPaise()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            // Map billingInterval to durationType
            String durationType = "yearly".equalsIgnoreCase(sub.getBillingInterval()) ? "ANNUAL" : "MONTHLY";

            CaliperPayment payment = CaliperPayment.builder()
                    .clientId(sub.getClientId())
                    .paymentId(paymentEntity.optString("id", ""))
                    .orderId(rzpSubId)
                    .amount(amountPaise / 100.0)
                    .status(CaliperPayment.PAYMENT_SUCCESS)
                    .planId(sub.getPlanId() != null ? sub.getPlanId() : 0L)
                    .paymentChannelType("RAZORPAY_AUTOPAY")
                    .paymentMethod(paymentEntity.optString("method", "autopay"))
                    .transactionDatetime(new Date())
                    .webhookEventId(eventId)
                    .durationType(durationType)
                    .cgstAmount(cgstRupees)
                    .sgstAmount(sgstRupees)
                    .build();

            try {
                caliperPaymentRepository.save(payment);
            } catch (DataIntegrityViolationException e) {
                log.warn("Duplicate subscription.charged event {} for sub {}, ignoring", eventId, rzpSubId);
                return;
            }

            // Keep plan active
            if (sub.getPlanId() != null) {
                planRepository.findById(sub.getPlanId()).ifPresent(plan -> {
                    plan.setStatus(Plan.STATUS_ACTIVE);
                    planRepository.save(plan);
                });
            }

            log.info("Subscription {} charged ₹{} for client {}", rzpSubId, amountPaise / 100.0, sub.getClientId());
        });
    }

    // -------------------------------------------------------------------
    // subscription.halted — payment retries exhausted
    // -------------------------------------------------------------------
    private void handleSubscriptionHalted(JSONObject event) {
        JSONObject subEntity = event.getJSONObject("payload").getJSONObject("subscription").getJSONObject("entity");
        String rzpSubId = subEntity.getString("id");

        subscriptionRepository.findByRazorpaySubscriptionId(rzpSubId).ifPresent(sub -> {
            sub.setStatus(RazorpaySubscription.STATUS_HALTED);
            sub.setUpdatedAt(new Date());
            subscriptionRepository.save(sub);

            if (sub.getPlanId() != null) {
                planRepository.findById(sub.getPlanId()).ifPresent(plan -> {
                    plan.setStatus(Plan.STATUS_INACTIVE);
                    planRepository.save(plan);
                });
            }

            sendHaltedEmail(sub.getClientId());
            log.warn("Subscription {} halted for client {}", rzpSubId, sub.getClientId());
        });
    }

    // -------------------------------------------------------------------
    // subscription.cancelled
    // -------------------------------------------------------------------
    private void handleSubscriptionCancelled(JSONObject event) {
        JSONObject subEntity = event.getJSONObject("payload").getJSONObject("subscription").getJSONObject("entity");
        String rzpSubId = subEntity.getString("id");

        subscriptionRepository.findByRazorpaySubscriptionId(rzpSubId).ifPresent(sub -> {
            sub.setStatus(RazorpaySubscription.STATUS_CANCELLED);
            sub.setUpdatedAt(new Date());
            subscriptionRepository.save(sub);

            if (sub.getPlanId() != null) {
                planRepository.findById(sub.getPlanId()).ifPresent(plan -> {
                    plan.setStatus(Plan.STATUS_INACTIVE);
                    planRepository.save(plan);
                });
            }

            log.info("Subscription {} cancelled for client {}", rzpSubId, sub.getClientId());
        });
    }

    // -------------------------------------------------------------------
    // payment.failed — only for subscription-linked payments
    // -------------------------------------------------------------------
    private void handlePaymentFailed(JSONObject event) {
        JSONObject paymentEntity = event.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");

        if (!paymentEntity.has("subscription_id") || paymentEntity.isNull("subscription_id")) return;

        String rzpSubId = paymentEntity.getString("subscription_id");
        String paymentId = paymentEntity.optString("id", "");
        double amount = paymentEntity.has("amount") ? paymentEntity.getDouble("amount") / 100.0 : 0.0;
        String errorDesc = paymentEntity.optString("error_description", "Payment failed");

        subscriptionRepository.findByRazorpaySubscriptionId(rzpSubId).ifPresent(sub -> {
            CaliperPayment payment = CaliperPayment.builder()
                    .clientId(sub.getClientId())
                    .paymentId(paymentId)
                    .orderId(rzpSubId)
                    .amount(amount)
                    .status(CaliperPayment.PAYMENT_FAILED)
                    .planId(sub.getPlanId() != null ? sub.getPlanId() : 0L)
                    .paymentChannelType("RAZORPAY_AUTOPAY")
                    .paymentMethod(paymentEntity.optString("method", "autopay"))
                    .transactionDatetime(new Date())
                    .errorDescription(errorDesc)
                    .build();
            caliperPaymentRepository.save(payment);

            sendPaymentFailedEmail(sub.getClientId(), errorDesc);
            log.warn("Payment failed for subscription {} client {}: {}", rzpSubId, sub.getClientId(), errorDesc);
        });
    }

    // -------------------------------------------------------------------
    // INTERNAL: activate the linked Caliper plan
    // -------------------------------------------------------------------
    private void activateCaliperPlan(RazorpaySubscription sub) {
        if (sub.getPlanId() != null) {
            planRepository.findById(sub.getPlanId()).ifPresent(plan -> {
                plan.setStatus(Plan.STATUS_ACTIVE);
                planRepository.save(plan);
            });
        }
    }

    // -------------------------------------------------------------------
    // INTERNAL: email helpers
    // -------------------------------------------------------------------
    private void sendHaltedEmail(String clientId) {
        clientRepository.findByClientId(clientId).ifPresent(client -> {
            try {
                String subject = "Action Required: Your Caliper subscription payment has failed";
                String html = "<h2>Subscription Payment Failed</h2>"
                        + "<p>Hi " + client.getClientName() + ",</p>"
                        + "<p>We were unable to collect your subscription payment after multiple attempts. "
                        + "Your Caliper plan has been temporarily paused.</p>"
                        + "<p>Please update your payment method or contact support to resume your subscription.</p>";
                emailService.sendHtmlEmail(client.getEmail(), subject, html);
            } catch (Exception e) {
                log.error("Failed to send halted email to client {}: {}", clientId, e.getMessage());
            }
        });
    }

    private void sendPaymentFailedEmail(String clientId, String reason) {
        clientRepository.findByClientId(clientId).ifPresent(client -> {
            try {
                String subject = "Caliper Autopay: Payment attempt failed";
                String html = "<h2>Payment Attempt Failed</h2>"
                        + "<p>Hi " + client.getClientName() + ",</p>"
                        + "<p>A payment attempt for your Caliper subscription failed. Razorpay will retry automatically.</p>"
                        + "<p>Reason: <em>" + reason + "</em></p>"
                        + "<p>If the issue persists, please update your payment mandate.</p>";
                emailService.sendHtmlEmail(client.getEmail(), subject, html);
            } catch (Exception e) {
                log.error("Failed to send payment failed email to client {}: {}", clientId, e.getMessage());
            }
        });
    }
}

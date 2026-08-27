package com.caliper.razorpay.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.caliper.location.entity.Client;
import com.caliper.location.repository.ClientRepository;
import com.caliper.planmanagement.dto.GstBreakdownDto;
import com.caliper.planmanagement.entity.Plan;
import com.caliper.planmanagement.entity.payment.RazorpayOrder;
import com.caliper.planmanagement.repository.PlanRepository;
import com.caliper.planmanagement.service.PricingService;
import com.caliper.razorpay.dto.CancelSubscriptionRequest;
import com.caliper.razorpay.dto.CreateSubscriptionRequest;
import com.caliper.razorpay.dto.SubscriptionResponse;
import com.caliper.razorpay.entity.RazorpayPlanMapping;
import com.caliper.razorpay.entity.RazorpaySubscription;
import com.caliper.razorpay.repository.RazorpayPlanMappingRepository;
import com.caliper.razorpay.repository.RazorpaySubscriptionRepository;
import com.caliper.utils.email.EmailService;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Subscription;

@Service
public class RazorpaySubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(RazorpaySubscriptionService.class);

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private RazorpaySubscriptionRepository subscriptionRepository;

    @Autowired
    private RazorpayPlanMappingRepository planMappingRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PricingService pricingService;

    // -------------------------------------------------------------------
    // CREATE SUBSCRIPTION
    // -------------------------------------------------------------------
    public ResponseEntity<SubscriptionResponse> createSubscription(CreateSubscriptionRequest req) {
        try {
            if (req.getClientId() == null || req.getCaliperServiceId() == null || req.getBillingInterval() == null) {
                return errorResponse(HttpStatus.BAD_REQUEST, "client_id, caliper_service_id, and billing_interval are required");
            }

            Optional<Client> clientOpt = clientRepository.findByClientId(req.getClientId());
            if (clientOpt.isEmpty()) {
                return errorResponse(HttpStatus.NOT_FOUND, "Client not found: " + req.getClientId());
            }
            Client client = clientOpt.get();

            List<String> activeStatuses = Arrays.asList(
                    RazorpaySubscription.STATUS_CREATED,
                    RazorpaySubscription.STATUS_AUTHENTICATED,
                    RazorpaySubscription.STATUS_ACTIVE);
            if (subscriptionRepository.existsByClientIdAndStatusIn(req.getClientId(), activeStatuses)) {
                return errorResponse(HttpStatus.CONFLICT, "An active subscription already exists for this client");
            }

            Optional<RazorpayPlanMapping> mappingOpt = planMappingRepository
                    .findByCaliperServiceIdAndBillingInterval(req.getCaliperServiceId(), req.getBillingInterval());
            if (mappingOpt.isEmpty()) {
                return errorResponse(HttpStatus.NOT_FOUND,
                        "No Razorpay plan found for service ID " + req.getCaliperServiceId()
                        + " with interval " + req.getBillingInterval()
                        + ". Please seed the razorpay_plan_mapping table.");
            }
            RazorpayPlanMapping mapping = mappingOpt.get();

            int totalCount = req.getTotalCount() != null ? req.getTotalCount()
                    : ("yearly".equalsIgnoreCase(req.getBillingInterval()) ? 3 : 12);

            // Compute GST on base amount
            GstBreakdownDto gst = pricingService.computeGst(mapping.getAmountPaise());

            JSONObject subscriptionRequest = new JSONObject();
            subscriptionRequest.put("plan_id", mapping.getRazorpayPlanId());
            subscriptionRequest.put("total_count", totalCount);
            subscriptionRequest.put("quantity", 1);
            subscriptionRequest.put("customer_notify", 1);
            subscriptionRequest.put("notes", new JSONObject()
                    .put("client_id", req.getClientId())
                    .put("client_name", client.getClientName()));

            Subscription rzpSubscription = razorpayClient.subscriptions.create(subscriptionRequest);

            String subscriptionId = rzpSubscription.get("id");
            String authLink = rzpSubscription.has("short_url") ? rzpSubscription.get("short_url") : "";
            String rzpStatus = rzpSubscription.get("status");

            Plan plan = planRepository.findByClientId(req.getClientId());
            Long planId = plan != null ? plan.getId() : null;

            RazorpaySubscription localSub = RazorpaySubscription.builder()
                    .clientId(req.getClientId())
                    .planId(planId)
                    .razorpaySubscriptionId(subscriptionId)
                    .razorpayPlanId(mapping.getRazorpayPlanId())
                    .status(rzpStatus)
                    .authLink(authLink)
                    .totalCount(totalCount)
                    .paidCount(0)
                    .billingInterval(req.getBillingInterval())
                    .amount(mapping.getAmountPaise() / 100.0)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            subscriptionRepository.save(localSub);

            sendAuthLinkEmail(client, authLink, req.getBillingInterval(), gst);

            log.info("Razorpay subscription created: {} for client: {}", subscriptionId, req.getClientId());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    buildResponse(HttpStatus.CREATED.value(),
                            "Subscription created. Auth link sent to client email.",
                            req.getClientId(), subscriptionId, authLink, rzpStatus,
                            req.getBillingInterval(), gst, 0, totalCount, null));

        } catch (RazorpayException e) {
            log.error("Razorpay API error for client {}: {}", req.getClientId(), e.getMessage());
            return errorResponse(HttpStatus.BAD_GATEWAY, "Razorpay error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating subscription for client {}: {}", req.getClientId(), e.getMessage());
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------
    // GET SUBSCRIPTION STATUS
    // -------------------------------------------------------------------
    public ResponseEntity<SubscriptionResponse> getSubscription(String clientId) {
        Optional<RazorpaySubscription> subOpt = subscriptionRepository.findByClientId(clientId);
        if (subOpt.isEmpty()) {
            return errorResponse(HttpStatus.NOT_FOUND, "No subscription found for client: " + clientId);
        }

        RazorpaySubscription sub = subOpt.get();

        if (!RazorpaySubscription.STATUS_CANCELLED.equals(sub.getStatus())
                && !RazorpaySubscription.STATUS_EXPIRED.equals(sub.getStatus())) {
            syncFromRazorpay(sub);
        }

        // Compute GST for current amount
        long amountPaise = Math.round(sub.getAmount() * 100);
        GstBreakdownDto gst = pricingService.computeGst(amountPaise);

        return ResponseEntity.ok(
                buildResponse(HttpStatus.OK.value(), "Subscription fetched successfully",
                        clientId, sub.getRazorpaySubscriptionId(), sub.getAuthLink(),
                        sub.getStatus(), sub.getBillingInterval(), gst,
                        sub.getPaidCount(), sub.getTotalCount(), sub.getNextChargeAt()));
    }

    // -------------------------------------------------------------------
    // CANCEL SUBSCRIPTION
    // -------------------------------------------------------------------
    public ResponseEntity<SubscriptionResponse> cancelSubscription(CancelSubscriptionRequest req) {
        try {
            Optional<RazorpaySubscription> subOpt = subscriptionRepository.findByClientId(req.getClientId());
            if (subOpt.isEmpty()) {
                return errorResponse(HttpStatus.NOT_FOUND, "No subscription found for client: " + req.getClientId());
            }

            RazorpaySubscription sub = subOpt.get();

            if (RazorpaySubscription.STATUS_CANCELLED.equals(sub.getStatus())) {
                return errorResponse(HttpStatus.CONFLICT, "Subscription is already cancelled");
            }

            JSONObject cancelRequest = new JSONObject();
            cancelRequest.put("cancel_at_cycle_end", req.isCancelAtCycleEnd() ? 1 : 0);

            razorpayClient.subscriptions.cancel(sub.getRazorpaySubscriptionId(), cancelRequest);

            sub.setStatus(RazorpaySubscription.STATUS_CANCELLED);
            sub.setUpdatedAt(new Date());
            subscriptionRepository.save(sub);

            if (sub.getPlanId() != null) {
                planRepository.findById(sub.getPlanId()).ifPresent(plan -> {
                    plan.setStatus(Plan.STATUS_INACTIVE);
                    planRepository.save(plan);
                });
            }

            log.info("Subscription cancelled: {} for client: {}", sub.getRazorpaySubscriptionId(), req.getClientId());

            return ResponseEntity.ok(
                    SubscriptionResponse.builder()
                            .status(HttpStatus.OK.value())
                            .message("Subscription cancelled successfully")
                            .clientId(req.getClientId())
                            .razorpaySubscriptionId(sub.getRazorpaySubscriptionId())
                            .subscriptionStatus(RazorpaySubscription.STATUS_CANCELLED)
                            .timestamp(new Date())
                            .build());

        } catch (RazorpayException e) {
            log.error("Razorpay API error cancelling for client {}: {}", req.getClientId(), e.getMessage());
            return errorResponse(HttpStatus.BAD_GATEWAY, "Razorpay error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error cancelling subscription for client {}: {}", req.getClientId(), e.getMessage());
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------
    // INTERNAL: sync status from Razorpay API
    // -------------------------------------------------------------------
    public void syncFromRazorpay(RazorpaySubscription sub) {
        try {
            Subscription rzpSub = razorpayClient.subscriptions.fetch(sub.getRazorpaySubscriptionId());
            sub.setStatus(rzpSub.get("status"));
            if (rzpSub.has("paid_count")) sub.setPaidCount(rzpSub.get("paid_count"));
            if (rzpSub.has("current_start") && rzpSub.get("current_start") != null)
                sub.setCurrentStart(new Date(((Number) rzpSub.get("current_start")).longValue() * 1000L));
            if (rzpSub.has("current_end") && rzpSub.get("current_end") != null)
                sub.setCurrentEnd(new Date(((Number) rzpSub.get("current_end")).longValue() * 1000L));
            if (rzpSub.has("charge_at") && rzpSub.get("charge_at") != null)
                sub.setNextChargeAt(new Date(((Number) rzpSub.get("charge_at")).longValue() * 1000L));
            sub.setUpdatedAt(new Date());
            subscriptionRepository.save(sub);
        } catch (RazorpayException e) {
            log.warn("Failed to sync subscription {} from Razorpay: {}", sub.getRazorpaySubscriptionId(), e.getMessage());
        }
    }

    // -------------------------------------------------------------------
    // INTERNAL: build standardised response with GST breakdown
    // -------------------------------------------------------------------
    private SubscriptionResponse buildResponse(int statusCode, String message,
            String clientId, String subId, String authLink, String subStatus,
            String billingInterval, GstBreakdownDto gst,
            Integer paidCount, Integer totalCount, Date nextChargeAt) {

        return SubscriptionResponse.builder()
                .status(statusCode)
                .message(message)
                .clientId(clientId)
                .razorpaySubscriptionId(subId)
                .authLink(authLink)
                .subscriptionStatus(subStatus)
                .billingInterval(billingInterval)
                .baseAmountRupees(gst.getBaseAmountPaise() / 100.0)
                .cgstAmountRupees(gst.getCgstPaise() / 100.0)
                .sgstAmountRupees(gst.getSgstPaise() / 100.0)
                .totalAmountRupees(gst.getTotalAmountPaise() / 100.0)
                .cgstRate(gst.getCgstRate())
                .sgstRate(gst.getSgstRate())
                .paidCount(paidCount)
                .totalCount(totalCount)
                .nextChargeAt(nextChargeAt)
                .timestamp(new Date())
                .build();
    }

    // -------------------------------------------------------------------
    // INTERNAL: send auth link email with GST breakdown
    // -------------------------------------------------------------------
    private void sendAuthLinkEmail(Client client, String authLink, String billingInterval, GstBreakdownDto gst) {
        try {
            double base = gst.getBaseAmountPaise() / 100.0;
            double cgst = gst.getCgstPaise() / 100.0;
            double sgst = gst.getSgstPaise() / 100.0;
            double total = gst.getTotalAmountPaise() / 100.0;

            String subject = "Complete your Caliper subscription setup";
            String html = "<h2>Welcome to Caliper Autopay</h2>"
                    + "<p>Your <strong>" + billingInterval + "</strong> subscription has been created.</p>"
                    + "<table style='border-collapse:collapse;'>"
                    + "<tr><td style='padding:4px 12px;'>Base Amount</td><td>₹" + String.format("%.2f", base) + "</td></tr>"
                    + "<tr><td style='padding:4px 12px;'>CGST (" + gst.getCgstRate() + "%)</td><td>₹" + String.format("%.2f", cgst) + "</td></tr>"
                    + "<tr><td style='padding:4px 12px;'>SGST (" + gst.getSgstRate() + "%)</td><td>₹" + String.format("%.2f", sgst) + "</td></tr>"
                    + "<tr><td style='padding:4px 12px;'><strong>Total per cycle</strong></td><td><strong>₹" + String.format("%.2f", total) + "</strong></td></tr>"
                    + "</table>"
                    + "<br><p>Click below to authorize your payment mandate (UPI / Card / NACH):</p>"
                    + "<p><a href=\"" + authLink + "\" style=\"background:#4F46E5;color:#fff;padding:12px 24px;text-decoration:none;border-radius:6px;\">Authorize Autopay</a></p>"
                    + "<p style='color:#888;font-size:12px;'>This link expires in 24 hours. Contact support if you did not request this.</p>";
            emailService.sendHtmlEmail(client.getEmail(), subject, html);
        } catch (Exception e) {
            log.error("Failed to send auth link email to {}: {}", client.getEmail(), e.getMessage());
        }
    }

    // -------------------------------------------------------------------
    // AUTO-ENROL AUTOPAY AFTER ONE-TIME PAYMENT
    // -------------------------------------------------------------------
    public Optional<RazorpaySubscription> enrollAutopayAfterPayment(RazorpayOrder order, Long planId) {
        try {
            List<String> activeStatuses = Arrays.asList(
                    RazorpaySubscription.STATUS_CREATED,
                    RazorpaySubscription.STATUS_AUTHENTICATED,
                    RazorpaySubscription.STATUS_ACTIVE);
            if (subscriptionRepository.existsByClientIdAndStatusIn(order.getClientId(), activeStatuses)) {
                log.info("Active subscription already exists for client {}, skipping autopay enrolment", order.getClientId());
                return Optional.empty();
            }

            Optional<Client> clientOpt = clientRepository.findByClientId(order.getClientId());
            if (clientOpt.isEmpty()) {
                log.error("Client not found for autopay enrolment: {}", order.getClientId());
                return Optional.empty();
            }
            Client client = clientOpt.get();

            DurationMapping dm = mapDurationType(order.getDurationType());

            long cgst = order.getCgstPaise() != null ? order.getCgstPaise() : 0L;
            long sgst = order.getSgstPaise() != null ? order.getSgstPaise() : 0L;
            long baseAmountPaise = order.getAmountPaise() - cgst - sgst;
            GstBreakdownDto gst = pricingService.computeGst(baseAmountPaise);

            String rzpPlanId = findOrCreateRazorpayPlan(
                    baseAmountPaise, gst.getTotalAmountPaise(), dm.billingInterval(), dm.period(), dm.interval());

            JSONObject subscriptionRequest = new JSONObject();
            subscriptionRequest.put("plan_id", rzpPlanId);
            subscriptionRequest.put("total_count", dm.totalCount());
            subscriptionRequest.put("quantity", 1);
            subscriptionRequest.put("customer_notify", 1);
            subscriptionRequest.put("notes", new JSONObject()
                    .put("client_id", order.getClientId())
                    .put("one_time_order_id", order.getRazorpayOrderId()));

            Subscription rzpSubscription = razorpayClient.subscriptions.create(subscriptionRequest);

            String subscriptionId = rzpSubscription.get("id");
            String authLink = rzpSubscription.has("short_url") ? rzpSubscription.get("short_url") : "";
            String rzpStatus = rzpSubscription.get("status");

            RazorpaySubscription localSub = RazorpaySubscription.builder()
                    .clientId(order.getClientId())
                    .planId(planId)
                    .razorpaySubscriptionId(subscriptionId)
                    .razorpayPlanId(rzpPlanId)
                    .status(rzpStatus)
                    .authLink(authLink)
                    .totalCount(dm.totalCount())
                    .paidCount(0)
                    .billingInterval(dm.billingInterval())
                    .amount(baseAmountPaise / 100.0)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            subscriptionRepository.save(localSub);
            sendAuthLinkEmail(client, authLink, dm.billingInterval(), gst);

            log.info("Autopay subscription {} enrolled for client {} after one-time payment",
                    subscriptionId, order.getClientId());
            return Optional.of(localSub);

        } catch (Exception e) {
            log.error("Autopay enrolment failed for client {}: {}", order.getClientId(), e.getMessage());
            return Optional.empty();
        }
    }

    // -------------------------------------------------------------------
    // INTERNAL: find or dynamically create a Razorpay Plan
    // -------------------------------------------------------------------
    private String findOrCreateRazorpayPlan(long baseAmountPaise, long totalAmountPaise,
            String billingInterval, String period, int interval) throws RazorpayException {

        Optional<RazorpayPlanMapping> existing =
                planMappingRepository.findByAmountPaiseAndBillingInterval(baseAmountPaise, billingInterval);
        if (existing.isPresent()) {
            return existing.get().getRazorpayPlanId();
        }

        JSONObject planRequest = new JSONObject();
        planRequest.put("period", period);
        planRequest.put("interval", interval);
        planRequest.put("item", new JSONObject()
                .put("name", "Caliper " + billingInterval + " Plan")
                .put("amount", totalAmountPaise)
                .put("currency", "INR"));

        com.razorpay.Plan rzpPlan = razorpayClient.plans.create(planRequest);
        String rzpPlanId = rzpPlan.get("id");

        try {
            RazorpayPlanMapping newMapping = RazorpayPlanMapping.builder()
                    .caliperServiceId(0L)
                    .billingInterval(billingInterval)
                    .razorpayPlanId(rzpPlanId)
                    .amountPaise(baseAmountPaise)
                    .currency("INR")
                    .createdAt(new Date())
                    .build();
            planMappingRepository.save(newMapping);
            log.info("Saved dynamic Razorpay Plan {} for baseAmountPaise={} billingInterval={}",
                    rzpPlanId, baseAmountPaise, billingInterval);
        } catch (DataIntegrityViolationException e) {
            log.warn("Could not cache plan mapping for billingInterval={} (duplicate key), plan {} will be used directly",
                    billingInterval, rzpPlanId);
        }
        return rzpPlanId;
    }

    // -------------------------------------------------------------------
    // INTERNAL: map Caliper durationType to Razorpay billing params
    // -------------------------------------------------------------------
    private record DurationMapping(String billingInterval, String period, int interval, int totalCount) {}

    private DurationMapping mapDurationType(String durationType) {
        if (durationType == null) return new DurationMapping("monthly", "monthly", 1, 12);
        return switch (durationType.toUpperCase()) {
            case "HALF_YEARLY" -> new DurationMapping("monthly", "monthly", 6, 2);
            case "ANNUAL"      -> new DurationMapping("yearly", "yearly", 1, 3);
            default            -> new DurationMapping("monthly", "monthly", 1, 12);
        };
    }

    // -------------------------------------------------------------------
    // INTERNAL: build error response
    // -------------------------------------------------------------------
    private ResponseEntity<SubscriptionResponse> errorResponse(HttpStatus httpStatus, String message) {
        return ResponseEntity.status(httpStatus).body(
                SubscriptionResponse.builder()
                        .status(httpStatus.value())
                        .message(message)
                        .timestamp(new Date())
                        .build());
    }
}

package com.caliper.planmanagement.service;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.caliper.onboarding.service.OnboardingService;
import com.caliper.planmanagement.dto.CreateOrderRequestDto;
import com.caliper.planmanagement.dto.CreateOrderResponseDto;
import com.caliper.planmanagement.dto.GstBreakdownDto;
import com.caliper.planmanagement.dto.VerifyPaymentRequestDto;
import com.caliper.planmanagement.dto.VerifyPaymentResponseDto;
import com.caliper.planmanagement.entity.Plan;
import com.caliper.planmanagement.entity.payment.RazorpayOrder;
import com.caliper.planmanagement.repository.payment.RazorpayOrderRepository;
import com.caliper.planmanagement.util.RazorpaySignatureUtil;
import com.caliper.razorpay.entity.RazorpaySubscription;
import com.caliper.razorpay.service.RazorpaySubscriptionService;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class RazorpayOrderService {

    private static final Logger log = LoggerFactory.getLogger(RazorpayOrderService.class);

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private RazorpayOrderRepository razorpayOrderRepository;

    @Autowired
    private PlanActivationService planActivationService;

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private RazorpaySubscriptionService razorpaySubscriptionService;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${gst.cgst.rate:9.0}")
    private double cgstRate;

    @Value("${gst.sgst.rate:9.0}")
    private double sgstRate;

    public CreateOrderResponseDto createOrder(CreateOrderRequestDto request) {
        validateCreateOrderRequest(request);

        // Idempotency: return existing non-expired CREATED order for the same client
        Optional<RazorpayOrder> existingOrder = razorpayOrderRepository
                .findByClientIdAndStatus(request.getClientId(), RazorpayOrder.STATUS_CREATED);

        if (existingOrder.isPresent() && existingOrder.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            RazorpayOrder order = existingOrder.get();
            log.info("Returning existing active order {} for clientId={}", order.getRazorpayOrderId(), request.getClientId());
            return buildResponseDto(order);
        }

        GstBreakdownDto gstBreakdown = pricingService.calculateWithGst(
                request.getServiceKeys(),
                request.getDurationType(),
                request.getLocationCount());

        if (gstBreakdown.getTotalAmountPaise() <= 0) {
            throw new InvalidRequestException("Calculated amount must be greater than zero");
        }

        Order razorpayApiOrder;
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", gstBreakdown.getTotalAmountPaise());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "rcpt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            orderRequest.put("payment_capture", 1);
            razorpayApiOrder = razorpayClient.orders.create(orderRequest);
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order: {}", e.getMessage());
            throw new InvalidRequestException("Payment gateway error: " + e.getMessage());
        }

        String razorpayOrderId = razorpayApiOrder.get("id");

        RazorpayOrder savedOrder = RazorpayOrder.builder()
                .razorpayOrderId(razorpayOrderId)
                .clientId(request.getClientId())
                .userId(request.getUserId())
                .amountPaise(gstBreakdown.getTotalAmountPaise())
                .cgstPaise(gstBreakdown.getCgstPaise())
                .sgstPaise(gstBreakdown.getSgstPaise())
                .currency("INR")
                .locationCount(request.getLocationCount())
                .durationType(request.getDurationType().toUpperCase())
                .serviceKeys(String.join(",", request.getServiceKeys()))
                .status(RazorpayOrder.STATUS_CREATED)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        razorpayOrderRepository.save(savedOrder);
        log.info("Razorpay order created: {} for clientId={}", razorpayOrderId, request.getClientId());

        return buildResponseDto(savedOrder);
    }

    public VerifyPaymentResponseDto verifyAndActivate(VerifyPaymentRequestDto request) {
        RazorpayOrder order = razorpayOrderRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + request.getRazorpayOrderId()));

        // Idempotency: already activated
        if (RazorpayOrder.STATUS_PAID.equals(order.getStatus())) {
            log.info("Order {} already paid, returning existing planId={}", order.getRazorpayOrderId(), order.getPlanId());
            String existingStep = onboardingService.getCurrentStepName(order.getClientId());
            return VerifyPaymentResponseDto.builder()
                    .success(true)
                    .planId(order.getPlanId())
                    .clientId(order.getClientId())
                    .onboardingStep(existingStep)
                    .message("Payment already verified")
                    .build();
        }

        if (RazorpayOrder.STATUS_FAILED.equals(order.getStatus()) ||
                RazorpayOrder.STATUS_EXPIRED.equals(order.getStatus())) {
            throw new InvalidRequestException("Order is in terminal state: " + order.getStatus());
        }

        boolean isValid;
        try {
            isValid = RazorpaySignatureUtil.verifyPaymentSignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature(),
                    keySecret);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Signature verification error: {}", e.getMessage());
            throw new InvalidRequestException("Signature verification failed");
        }

        if (!isValid) {
            order.setStatus(RazorpayOrder.STATUS_FAILED);
            razorpayOrderRepository.save(order);
            log.warn("Invalid payment signature for orderId={}", request.getRazorpayOrderId());
            throw new InvalidRequestException("Payment signature verification failed. Transaction rejected.");
        }

        Plan activatedPlan = planActivationService.activatePlan(
                order,
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());

        // Auto-enrol Autopay — best effort, failure does not affect payment response
        String autopaySubscriptionId = null;
        String autopayAuthLink = null;
        try {
            Optional<RazorpaySubscription> enrolled =
                    razorpaySubscriptionService.enrollAutopayAfterPayment(order, activatedPlan.getId());
            if (enrolled.isPresent()) {
                autopaySubscriptionId = enrolled.get().getRazorpaySubscriptionId();
                autopayAuthLink = enrolled.get().getAuthLink();
            }
        } catch (Exception e) {
            log.error("Autopay enrolment failed for clientId={}: {}", order.getClientId(), e.getMessage());
        }

        // Fetch the onboarding step determined during plan activation
        String onboardingStep = onboardingService.getCurrentStepName(order.getClientId());

        return VerifyPaymentResponseDto.builder()
                .success(true)
                .planId(activatedPlan.getId())
                .clientId(order.getClientId())
                .onboardingStep(onboardingStep)
                .message("Payment verified and plan activated successfully")
                .autopaySubscriptionId(autopaySubscriptionId)
                .autopayAuthLink(autopayAuthLink)
                .build();
    }

    private void validateCreateOrderRequest(CreateOrderRequestDto request) {
        if (request.getClientId() == null || request.getClientId().isBlank()) {
            throw new InvalidRequestException("clientId is required");
        }
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new InvalidRequestException("userId is required");
        }
        if (request.getServiceKeys() == null || request.getServiceKeys().isEmpty()) {
            throw new InvalidRequestException("At least one service must be selected");
        }
        if (request.getDurationType() == null || request.getDurationType().isBlank()) {
            throw new InvalidRequestException("durationType is required");
        }
        if (request.getLocationCount() <= 0) {
            throw new InvalidRequestException("locationCount must be greater than zero");
        }
    }

    private CreateOrderResponseDto buildResponseDto(RazorpayOrder order) {
        GstBreakdownDto gstBreakdown = null;
        if (order.getCgstPaise() != null && order.getSgstPaise() != null) {
            long base = order.getAmountPaise() - order.getCgstPaise() - order.getSgstPaise();
            gstBreakdown = GstBreakdownDto.builder()
                    .baseAmountPaise(base)
                    .cgstPaise(order.getCgstPaise())
                    .sgstPaise(order.getSgstPaise())
                    .totalAmountPaise(order.getAmountPaise())
                    .cgstRate(BigDecimal.valueOf(cgstRate))
                    .sgstRate(BigDecimal.valueOf(sgstRate))
                    .build();
        }
        return CreateOrderResponseDto.builder()
                .razorpayOrderId(order.getRazorpayOrderId())
                .amountPaise(order.getAmountPaise())
                .currency(order.getCurrency())
                .keyId(keyId)
                .clientId(order.getClientId())
                .gstBreakdown(gstBreakdown)
                .build();
    }
}

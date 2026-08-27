package com.caliper.planmanagement.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.campaign.google.entity.GoogleCampaign;
import com.caliper.campaign.google.repository.GoogleCampaignRepository;
import com.caliper.planmanagement.dto.CampaignCreateOrderRequestDto;
import com.caliper.planmanagement.dto.CampaignCreateOrderResponseDto;
import com.caliper.planmanagement.dto.CampaignPaymentBreakdownDto;
import com.caliper.planmanagement.dto.CampaignPricePreviewResponseDto;
import com.caliper.planmanagement.dto.CampaignVerifyPaymentRequestDto;
import com.caliper.planmanagement.dto.CampaignVerifyPaymentResponseDto;
import com.caliper.planmanagement.entity.payment.CaliperPayment;
import com.caliper.planmanagement.entity.payment.RazorpayOrder;
import com.caliper.planmanagement.repository.payment.CaliperPaymentRepository;
import com.caliper.planmanagement.repository.payment.RazorpayOrderRepository;
import com.caliper.planmanagement.util.RazorpaySignatureUtil;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class CampaignOrderService {

    private static final Logger log = LoggerFactory.getLogger(CampaignOrderService.class);

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private RazorpayOrderRepository razorpayOrderRepository;

    @Autowired
    private GoogleCampaignRepository googleCampaignRepository;

    @Autowired
    private CaliperPaymentRepository caliperPaymentRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public CampaignCreateOrderResponseDto createOrder(CampaignCreateOrderRequestDto request) {
        validateCreateOrderRequest(request);

        GoogleCampaign campaign = googleCampaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Campaign not found for campaignId: " + request.getCampaignId()));

        // Idempotency: return existing non-expired CREATED order for the same campaign
        Optional<RazorpayOrder> existingOrder = razorpayOrderRepository
                .findByCampaignIdAndStatus(request.getCampaignId(), RazorpayOrder.STATUS_CREATED);

        if (existingOrder.isPresent() && existingOrder.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            RazorpayOrder order = existingOrder.get();
            log.info("Returning existing active order {} for campaignId={}", order.getRazorpayOrderId(), request.getCampaignId());
            return buildResponseDto(order);
        }

        if (campaign.getTotalBudget() == null || campaign.getTotalBudget().signum() <= 0) {
            throw new InvalidRequestException("Campaign total budget must be greater than zero");
        }

        CampaignPaymentBreakdownDto breakdown = pricingService.computeCampaignPayable(campaign.getTotalBudget());
        long grandTotalPaise = breakdown.getGrandTotal() * 100L;

        if (grandTotalPaise <= 0) {
            throw new InvalidRequestException("Calculated amount must be greater than zero");
        }

        Order razorpayApiOrder;
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", grandTotalPaise);
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
                .campaignId(request.getCampaignId())
                .amountPaise(grandTotalPaise)
                .agencyCommissionPaise(breakdown.getAgencyCommission() * 100L)
                .cgstPaise(breakdown.getCgst() * 100L)
                .sgstPaise(breakdown.getSgst() * 100L)
                .currency("INR")
                .status(RazorpayOrder.STATUS_CREATED)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        razorpayOrderRepository.save(savedOrder);
        log.info("Razorpay order created: {} for campaignId={}", razorpayOrderId, request.getCampaignId());

        return buildResponseDto(savedOrder);
    }

    @Transactional
    public CampaignVerifyPaymentResponseDto verifyAndActivate(CampaignVerifyPaymentRequestDto request) {
        RazorpayOrder order = razorpayOrderRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + request.getRazorpayOrderId()));

        // Idempotency: already activated
        if (RazorpayOrder.STATUS_PAID.equals(order.getStatus())) {
            log.info("Order {} already paid for campaignId={}", order.getRazorpayOrderId(), order.getCampaignId());
            GoogleCampaign campaign = googleCampaignRepository.findById(order.getCampaignId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Campaign not found for campaignId: " + order.getCampaignId()));
            return CampaignVerifyPaymentResponseDto.builder()
                    .success(true)
                    .campaignId(order.getCampaignId())
                    .clientId(order.getClientId())
                    .campaignStatus(campaign.getStatus())
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

        GoogleCampaign campaign = googleCampaignRepository.findById(order.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Campaign not found for campaignId: " + order.getCampaignId()));

        campaign.setStatus(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAYMENT_SUCCESSFUL);
        googleCampaignRepository.save(campaign);

        BigDecimal cgstAmount = order.getCgstPaise() != null
                ? BigDecimal.valueOf(order.getCgstPaise()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : null;
        BigDecimal sgstAmount = order.getSgstPaise() != null
                ? BigDecimal.valueOf(order.getSgstPaise()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : null;
        BigDecimal agencyCommission = order.getAgencyCommissionPaise() != null
                ? BigDecimal.valueOf(order.getAgencyCommissionPaise()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : null;

        CaliperPayment payment = CaliperPayment.builder()
                .clientId(order.getClientId())
                .userId(order.getUserId())
                .paymentId(request.getRazorpayPaymentId())
                .orderId(order.getRazorpayOrderId())
                .amount(order.getAmountPaise() / 100.0)
                .status(CaliperPayment.PAYMENT_SUCCESS)
                .campaignId(order.getCampaignId())
                .transactionDatetime(new Date())
                .razorpaySignature(request.getRazorpaySignature())
                .cgstAmount(cgstAmount)
                .sgstAmount(sgstAmount)
                .agencyCommission(agencyCommission)
                .build();

        caliperPaymentRepository.save(payment);

        order.setStatus(RazorpayOrder.STATUS_PAID);
        razorpayOrderRepository.save(order);

        log.info("Campaign payment verified and activated for campaignId={}", order.getCampaignId());

        return CampaignVerifyPaymentResponseDto.builder()
                .success(true)
                .campaignId(order.getCampaignId())
                .clientId(order.getClientId())
                .campaignStatus(campaign.getStatus())
                .message("Payment verified and campaign activated successfully")
                .build();
    }

    public CampaignPricePreviewResponseDto getPricePreview(String clientId, long campaignId) {
        if (clientId == null || clientId.isBlank()) {
            throw new InvalidRequestException("clientId is required");
        }

        GoogleCampaign campaign = googleCampaignRepository.findById(campaignId)
                .filter(c -> clientId.equals(c.getClientId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Campaign not found for clientId=" + clientId + ", campaignId=" + campaignId));

        if (campaign.getTotalBudget() == null || campaign.getTotalBudget().signum() <= 0) {
            throw new InvalidRequestException("Campaign total budget must be greater than zero");
        }

        CampaignPaymentBreakdownDto breakdown = pricingService.computeCampaignPayable(campaign.getTotalBudget());

        return CampaignPricePreviewResponseDto.builder()
                .campaignId(campaignId)
                .clientId(campaign.getClientId())
                .breakdown(breakdown)
                .build();
    }

    private void validateCreateOrderRequest(CampaignCreateOrderRequestDto request) {
        if (request.getClientId() == null || request.getClientId().isBlank()) {
            throw new InvalidRequestException("clientId is required");
        }
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new InvalidRequestException("userId is required");
        }
        if (request.getCampaignId() <= 0) {
            throw new InvalidRequestException("campaignId is required");
        }
    }

    private CampaignCreateOrderResponseDto buildResponseDto(RazorpayOrder order) {
        CampaignPaymentBreakdownDto breakdown = null;
        if (order.getCgstPaise() != null && order.getSgstPaise() != null && order.getAgencyCommissionPaise() != null) {
            long agencyCommission = order.getAgencyCommissionPaise() / 100;
            long taxableValue = (order.getAmountPaise() - order.getCgstPaise() - order.getSgstPaise()) / 100;
            long campaignBudget = taxableValue - agencyCommission;
            breakdown = CampaignPaymentBreakdownDto.builder()
                    .campaignBudget(campaignBudget)
                    .agencyCommission(agencyCommission)
                    .taxableValue(taxableValue)
                    .sgst(order.getSgstPaise() / 100)
                    .cgst(order.getCgstPaise() / 100)
                    .grandTotal(order.getAmountPaise() / 100)
                    .build();
        }
        return CampaignCreateOrderResponseDto.builder()
                .razorpayOrderId(order.getRazorpayOrderId())
                .amountPaise(order.getAmountPaise())
                .currency(order.getCurrency())
                .keyId(keyId)
                .clientId(order.getClientId())
                .campaignId(order.getCampaignId())
                .breakdown(breakdown)
                .build();
    }
}

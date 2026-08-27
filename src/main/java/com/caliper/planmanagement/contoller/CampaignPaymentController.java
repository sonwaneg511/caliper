package com.caliper.planmanagement.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.planmanagement.dto.CampaignCreateOrderRequestDto;
import com.caliper.planmanagement.dto.CampaignCreateOrderResponseDto;
import com.caliper.planmanagement.dto.CampaignPricePreviewResponseDto;
import com.caliper.planmanagement.dto.CampaignVerifyPaymentRequestDto;
import com.caliper.planmanagement.dto.CampaignVerifyPaymentResponseDto;
import com.caliper.planmanagement.service.CampaignOrderService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/campaign/payment")
@Tag(name = "Payment", description = "Secure Razorpay payment operations for campaigns")
public class CampaignPaymentController {

    @Autowired
    private CampaignOrderService campaignOrderService;

    /**
     * Preview the amount + GST breakdown for a campaign's payment before creating a
     * Razorpay order. Read-only — no Razorpay API call, no order row persisted.
     */
    @GetMapping("/preview")
    public ResponseEntity<CampaignPricePreviewResponseDto> getPricePreview(
            @RequestParam String clientId, @RequestParam long campaignId) {
        CampaignPricePreviewResponseDto response = campaignOrderService.getPricePreview(clientId, campaignId);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 1: Create a Razorpay order server-side.
     * Returns orderId, amount and keyId (public key only — never keySecret).
     * Frontend uses these to open the Razorpay Checkout SDK.
     */
    @PostMapping("/create-order")
    public ResponseEntity<CampaignCreateOrderResponseDto> createOrder(@RequestBody CampaignCreateOrderRequestDto request) {
        CampaignCreateOrderResponseDto response = campaignOrderService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 2: Verify the payment signature after the user completes payment.
     * Backend performs HMAC-SHA256 verification using keySecret (never exposed to frontend).
     * On success, marks the campaign paid and returns its status.
     */
    @PostMapping("/verify")
    public ResponseEntity<CampaignVerifyPaymentResponseDto> verifyPayment(@RequestBody CampaignVerifyPaymentRequestDto request) {
        CampaignVerifyPaymentResponseDto response = campaignOrderService.verifyAndActivate(request);
        return ResponseEntity.ok(response);
    }
}

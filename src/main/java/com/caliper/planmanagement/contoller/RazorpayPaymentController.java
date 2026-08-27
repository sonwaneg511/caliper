package com.caliper.planmanagement.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.planmanagement.dto.CreateOrderRequestDto;
import com.caliper.planmanagement.dto.CreateOrderResponseDto;
import com.caliper.planmanagement.dto.VerifyPaymentRequestDto;
import com.caliper.planmanagement.dto.VerifyPaymentResponseDto;
import com.caliper.planmanagement.service.RazorpayOrderService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/plan/payment")
@Tag(name = "Payment", description = "Secure Razorpay payment operations")
public class RazorpayPaymentController {

    @Autowired
    private RazorpayOrderService razorpayOrderService;

    /**
     * Step 1: Create a Razorpay order server-side.
     * Returns orderId, amount and keyId (public key only — never keySecret).
     * Frontend uses these to open the Razorpay Checkout SDK.
     */
    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponseDto> createOrder(@RequestBody CreateOrderRequestDto request) {
        CreateOrderResponseDto response = razorpayOrderService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 2: Verify the payment signature after the user completes payment.
     * Backend performs HMAC-SHA256 verification using keySecret (never exposed to frontend).
     * On success, activates the plan and returns the planId.
     */
    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponseDto> verifyPayment(@RequestBody VerifyPaymentRequestDto request) {
        VerifyPaymentResponseDto response = razorpayOrderService.verifyAndActivate(request);
        return ResponseEntity.ok(response);
    }
}

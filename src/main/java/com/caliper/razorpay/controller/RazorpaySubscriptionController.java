package com.caliper.razorpay.controller;

import com.caliper.razorpay.dto.CancelSubscriptionRequest;
import com.caliper.razorpay.dto.CreateSubscriptionRequest;
import com.caliper.razorpay.dto.SubscriptionResponse;
import com.caliper.razorpay.service.RazorpaySubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/razorpay/subscription")
@Tag(name = "Razorpay Autopay", description = "Manage Razorpay recurring subscriptions")
public class RazorpaySubscriptionController {

    @Autowired
    private RazorpaySubscriptionService subscriptionService;

    /**
     * Create a new Razorpay Autopay subscription for a client.
     * Returns an auth_link that must be shared with the client to complete mandate registration.
     */
    @PostMapping("/create")
    public ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody CreateSubscriptionRequest request) {
        return subscriptionService.createSubscription(request);
    }

    /**
     * Get the current subscription status and next charge details for a client.
     */
    @GetMapping("/{clientId}")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable String clientId) {
        return subscriptionService.getSubscription(clientId);
    }

    /**
     * Cancel an active subscription for a client.
     * Set cancel_at_cycle_end=true to cancel gracefully at end of current billing cycle.
     */
    @PostMapping("/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(@RequestBody CancelSubscriptionRequest request) {
        return subscriptionService.cancelSubscription(request);
    }
}

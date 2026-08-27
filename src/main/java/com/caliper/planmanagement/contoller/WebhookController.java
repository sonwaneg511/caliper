package com.caliper.planmanagement.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.planmanagement.service.RazorpayWebhookService;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    @Autowired
    private RazorpayWebhookService razorpayWebhookService;

    /**
     * Public endpoint — no JWT required.
     * Razorpay sends POST with raw JSON body and X-Razorpay-Signature header.
     * Signature is verified server-side using the webhook secret.
     */
    @PostMapping(value = "/razorpay", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> razorpayWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        return razorpayWebhookService.handleWebhook(rawBody, signature);
    }
}

package com.caliper.planmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyPaymentRequestDto {

    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
    private String clientId;
}

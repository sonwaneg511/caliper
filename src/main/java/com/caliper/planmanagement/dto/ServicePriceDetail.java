package com.caliper.planmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServicePriceDetail {

    private String serviceName;
    private String serviceKey;
    private double pricePerLocation;
    private double subtotal;
}

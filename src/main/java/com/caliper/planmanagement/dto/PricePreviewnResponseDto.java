package com.caliper.planmanagement.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PricePreviewnResponseDto {

    private List<ServicePriceDetail> serviceBreakdown;
    private long locationCount;
    private String durationType;
    private double totalAmountRupees;
    private long totalAmountPaise;
    private GstBreakdownDto gstBreakdown;
}

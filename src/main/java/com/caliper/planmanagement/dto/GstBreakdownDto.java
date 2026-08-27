package com.caliper.planmanagement.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GstBreakdownDto {

    private long baseAmountPaise;
    private long cgstPaise;
    private long sgstPaise;
    private long totalAmountPaise;
    private BigDecimal cgstRate;
    private BigDecimal sgstRate;
}

package com.caliper.planmanagement.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.caliper.planmanagement.dto.CampaignPaymentBreakdownDto;
import com.caliper.planmanagement.dto.GstBreakdownDto;
import com.caliper.planmanagement.dto.PricePreviewnResponseDto;
import com.caliper.planmanagement.dto.ServicePriceDetail;
import com.caliper.planmanagement.entity.PlanDurationType;
import com.caliper.planmanagement.entity.PlanServiceMaster;
import com.caliper.planmanagement.repository.PlanServiceMasterRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;

@Service
public class PricingService {

    @Autowired
    private PlanServiceMasterRepository planServiceMasterRepository;

    @Value("${gst.cgst.rate:9.0}")
    private double cgstRate;

    @Value("${gst.sgst.rate:9.0}")
    private double sgstRate;

    @Value("${campaign.commission.rate:10.0}")
    private double commissionRate;

    public CampaignPaymentBreakdownDto computeCampaignPayable(BigDecimal campaignBudget) {
        BigDecimal commission = campaignBudget.multiply(BigDecimal.valueOf(commissionRate))
                .divide(BigDecimal.valueOf(100));
        BigDecimal taxableValue = campaignBudget.add(commission);
        BigDecimal sgst = taxableValue.multiply(BigDecimal.valueOf(sgstRate)).divide(BigDecimal.valueOf(100));
        BigDecimal cgst = taxableValue.multiply(BigDecimal.valueOf(cgstRate)).divide(BigDecimal.valueOf(100));
        BigDecimal grandTotal = taxableValue.add(sgst).add(cgst);

        return CampaignPaymentBreakdownDto.builder()
                .campaignBudget(roundToRupees(campaignBudget))
                .agencyCommission(roundToRupees(commission))
                .taxableValue(roundToRupees(taxableValue))
                .sgst(roundToRupees(sgst))
                .cgst(roundToRupees(cgst))
                .grandTotal(roundToRupees(grandTotal))
                .build();
    }

    private long roundToRupees(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public PricePreviewnResponseDto getPricePreview(
            List<String> serviceKeys,
            String durationType,
            long locationCount) {

        PlanDurationType type = parseDurationType(durationType);
        List<PlanServiceMaster> services = planServiceMasterRepository.findAllByServiceKeyIn(serviceKeys);

        if (services.isEmpty()) {
            throw new ResourceNotFoundException("No matching services found for keys: " + serviceKeys);
        }

        List<ServicePriceDetail> breakdown = new ArrayList<>();
        double totalRupees = 0.0;

        for (PlanServiceMaster service : services) {
            double pricePerLocation = getPriceForDuration(service, type);
            double subtotal = pricePerLocation * locationCount;
            totalRupees += subtotal;

            breakdown.add(ServicePriceDetail.builder()
                    .serviceName(service.getServiceName())
                    .serviceKey(service.getServiceKey())
                    .pricePerLocation(pricePerLocation)
                    .subtotal(subtotal)
                    .build());
        }

        long basePaise = Math.round(totalRupees * 100);
        GstBreakdownDto gstBreakdown = computeGst(basePaise);

        return PricePreviewnResponseDto.builder()
                .serviceBreakdown(breakdown)
                .locationCount(locationCount)
                .durationType(durationType)
                .totalAmountRupees(totalRupees)
                .totalAmountPaise(basePaise)
                .gstBreakdown(gstBreakdown)
                .build();
    }

    public GstBreakdownDto calculateWithGst(List<String> serviceKeys, String durationType, long locationCount) {
        PricePreviewnResponseDto preview = getPricePreview(serviceKeys, durationType, locationCount);
        return preview.getGstBreakdown();
    }

    public long calculateTotalPaise(List<String> serviceKeys, String durationType, long locationCount) {
        PricePreviewnResponseDto preview = getPricePreview(serviceKeys, durationType, locationCount);
        return preview.getTotalAmountPaise();
    }

    public GstBreakdownDto computeGst(long basePaise) {
        long cgstPaise = Math.round(basePaise * cgstRate / 100.0);
        long sgstPaise = Math.round(basePaise * sgstRate / 100.0);
        long totalPaise = basePaise + cgstPaise + sgstPaise;
        return GstBreakdownDto.builder()
                .baseAmountPaise(basePaise)
                .cgstPaise(cgstPaise)
                .sgstPaise(sgstPaise)
                .totalAmountPaise(totalPaise)
                .cgstRate(BigDecimal.valueOf(cgstRate))
                .sgstRate(BigDecimal.valueOf(sgstRate))
                .build();
    }

    private double getPriceForDuration(PlanServiceMaster service, PlanDurationType type) {
        return switch (type) {
            case MONTHLY -> service.getMonthly() != null ? service.getMonthly() : 0.0;
            case HALF_YEARLY -> service.getHalfYearly() != null ? service.getHalfYearly() : 0.0;
            case ANNUAL -> service.getYearly() != null ? service.getYearly() : 0.0;
        };
    }

    private PlanDurationType parseDurationType(String durationType) {
        try {
            return PlanDurationType.valueOf(durationType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(
                    "Invalid durationType '" + durationType + "'. Allowed values: MONTHLY, HALF_YEARLY, ANNUAL");
        }
    }
}

package com.caliper.planmanagement.contoller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.dto.PageableResponse;
import com.caliper.planmanagement.dto.CampaignPaymentReportRequest;
import com.caliper.planmanagement.dto.CampaignPaymentReportResponse;
import com.caliper.planmanagement.dto.PaymentPlanResponse;
import com.caliper.planmanagement.dto.PlanRequestDto;
import com.caliper.planmanagement.dto.PlanResponseDto;
import com.caliper.planmanagement.dto.PricePreviewnRequestDto;
import com.caliper.planmanagement.dto.PricePreviewnResponseDto;
import com.caliper.planmanagement.dto.SubscriptionDetailResponse;
import com.caliper.planmanagement.dto.SubscriptionHistoryResponse;
import com.caliper.planmanagement.entity.PlanHistory;
import com.caliper.planmanagement.entity.PlanServiceMaster;
import com.caliper.planmanagement.service.PlanService;
import com.caliper.planmanagement.service.PricingService;

import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/plan")
@Tag(name = "Plan", description = "Operations related to plan management")
public class PlanController {

	@Autowired
	public PlanService planService;

	@Autowired
	public PricingService pricingService;

	@GetMapping("/allplans")
	public ResponseEntity<List<PlanServiceMaster>> getAllPlanServices() {
	    return planService.showPlans();
	}

	@GetMapping("/subscription/{clientId}")
	public ResponseEntity<SubscriptionDetailResponse> getSubscriptionDetail(@PathVariable String clientId) {
	    return ResponseEntity.ok(planService.getSubscriptionDetail(clientId));
	}

	@GetMapping("/history/{clientId}")
	public ResponseEntity<?> getSubscriptionHistory(
	        @PathVariable String clientId,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		Page<SubscriptionHistoryResponse> subscriptionHistory = planService.getSubscriptionHistory(clientId, page, size);
		PageableResponse<SubscriptionHistoryResponse> response = new PageableResponse<>(subscriptionHistory);
	    return ResponseEntity.ok(response);
	}

	@PostMapping("/createplan")
	public ResponseEntity<PlanResponseDto> createPlan(@RequestBody PlanRequestDto dto) {
	    return planService.createPlan(dto);
	}

	@PostMapping("/createpayment")
	public ResponseEntity<PaymentPlanResponse> createPayment(@RequestBody PlanRequestDto dto) {
	    return planService.createPayment(dto);
	}

	@PostMapping("/price-preview")
	public ResponseEntity<PricePreviewnResponseDto> getPricePreview(@RequestBody PricePreviewnRequestDto previewnRequestDto) {
	    PricePreviewnResponseDto preview = pricingService.getPricePreview(previewnRequestDto.getServiceKeys(), previewnRequestDto.getDurationType(), previewnRequestDto.getLocationCount()	);
	    return ResponseEntity.ok(preview);
	}

	@PostMapping("/campaign-payments")
	public ResponseEntity<CampaignPaymentReportResponse> getCampaignPaymentsForUser(
			@RequestBody CampaignPaymentReportRequest request, Authentication authentication) {
	    return planService.getCampaignPaymentsForUser(request, authentication);
	}
}

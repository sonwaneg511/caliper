package com.caliper.onboarding.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.onboarding.dto.AccessAccountDto;
import com.caliper.onboarding.dto.OnboardingStatusResponse;
import com.caliper.onboarding.dto.SkipConnectionRequest;
import com.caliper.onboarding.service.OnboardingService;
import com.caliper.utils.exception.customException.InvalidRequestException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/onboarding")
@Tag(name = "Onboarding", description = "Operations related to post-payment onboarding flow")
public class OnboardingController {

	@Autowired
    private OnboardingService onboardingService;

	@PostMapping("/insert-access-account")
    public ResponseEntity<String> insertAccessAccount(@RequestBody AccessAccountDto accessAccountDto) {
        	onboardingService.insertAccessAccount(accessAccountDto);
            return ResponseEntity.ok("Access Account inserted successfully");
    }

	@GetMapping("/status")
	@Operation(summary = "Get onboarding status", description = "Returns the current onboarding step and per-platform connection statuses.")
	public ResponseEntity<OnboardingStatusResponse> getStatus(@RequestParam("clientId") String clientId) {
		OnboardingStatusResponse status = onboardingService.getStatus(clientId);
		if (status == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(status);
	}

	@PostMapping("/skip-connection")
	@Operation(summary = "Skip a social platform connection", description = "Marks GMB or Meta as SKIPPED. At least one platform must eventually be CONNECTED.")
	public ResponseEntity<?> skipConnection(@RequestBody SkipConnectionRequest request) {
		if (request.getClientId() == null || request.getClientId().isBlank()) {
			throw new InvalidRequestException("client_id is required");
		}
		if (request.getPlatform() == null || request.getPlatform().isBlank()) {
			throw new InvalidRequestException("platform is required (GMB or META)");
		}
		onboardingService.skipPlatformConnection(request.getClientId(), request.getPlatform());
		return ResponseEntity.ok(onboardingService.getStatus(request.getClientId()));
	}

	@PostMapping("/complete-campaign-setup")
	@Operation(summary = "Complete campaign setup step", description = "Marks the campaign setup as done and advances onboarding to COMPLETED.")
	public ResponseEntity<OnboardingStatusResponse> completeCampaignSetup(@RequestBody Map<String, String> body) {
		String clientId = body.get("clientId");
		if (clientId == null || clientId.isBlank()) {
			throw new InvalidRequestException("clientId is required");
		}
		onboardingService.completeCampaignSetup(clientId);
		return ResponseEntity.ok(onboardingService.getStatus(clientId));
	}
}

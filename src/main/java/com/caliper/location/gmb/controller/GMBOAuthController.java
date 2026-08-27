package com.caliper.location.gmb.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.location.gmb.service.GMBOAuthService;
import com.caliper.onboarding.service.OnboardingService;

@RestController
@RequestMapping("/gmb-oauth")
public class GMBOAuthController {

	@Autowired
	private GMBOAuthService gmbOAuthService;

	@Autowired
	private OnboardingService onboardingService;
	
	@Value("${gmb.connection.success.url}")
	private String gmbConSuccessUrl;

	/**
	 * GET /gmb-oauth/authorize?client_id=CLIENT_ID&user_id=USER_ID
	 */
	@GetMapping("/authorize")
	public ResponseEntity<String> authorize(@RequestParam("client_id") String clientId,
			@RequestParam("user_id") String userId) {
		String authorizationUrl = gmbOAuthService.generateAuthorizationUrl(clientId, userId);
		return ResponseEntity.ok(authorizationUrl);
	}

	/**
	 * GET /gmb-oauth/callback?code=AUTH_CODE&state=ENCODED_STATE
	 */
	@GetMapping("/callback")
	public ResponseEntity<String> callback(@RequestParam("code") String code, @RequestParam("state") String state) {
		try {
			gmbOAuthService.handleOAuthCallback(code, state);
			return ResponseEntity.status(HttpStatus.FOUND)
					.location(URI.create(gmbConSuccessUrl + "?gmb=success")).build();
		} catch (Exception e) {
			try {
				String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
				String clientId = decoded.split(":", 2)[0];
				onboardingService.markPlatformFailed(clientId, "GMB");
			} catch (Exception ignored) { }
			return ResponseEntity.status(HttpStatus.FOUND)
					.location(URI.create(gmbConSuccessUrl + "?gmb=error&message=Failed+to+connect+GMB"))
					.build();
		}
	}

	/**
	 * GET /gmb-oauth/resync-accounts?client_id=CLIENT_ID&user_id=USER_ID
	 */
	@GetMapping("/resync-accounts")
	public ResponseEntity<String> resyncAccounts(@RequestParam("client_id") String clientId,
			@RequestParam("user_id") String userId) {
		try {
			gmbOAuthService.fetchAndSaveGMBAccounts(clientId, userId);
			return ResponseEntity.ok("GMB accounts re-synced successfully for clientId=" + clientId);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to re-sync GMB accounts: " + e.getMessage());
		}
	}
}

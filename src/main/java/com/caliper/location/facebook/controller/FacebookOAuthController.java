package com.caliper.location.facebook.controller;

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

import com.caliper.location.facebook.service.FacebookOAuthService;
import com.caliper.onboarding.service.OnboardingService;

@RestController
@RequestMapping("/facebook-oauth")
public class FacebookOAuthController {

	@Autowired
	private FacebookOAuthService facebookOAuthService;

	@Autowired
	private OnboardingService onboardingService;
	
	@Value("${facebook.connection.success.url}")
	private String facebookConUrl;

	/**
	 * GET /facebook-oauth/authorize?client_id=CLIENT_ID&user_id=USER_ID
	 */
	@GetMapping("/authorize")
	public ResponseEntity<String> authorize(@RequestParam("client_id") String clientId,
			@RequestParam("user_id") String userId) {
		String authorizationUrl = facebookOAuthService.generateAuthorizationUrl(clientId, userId);
		return ResponseEntity.ok(authorizationUrl);
	}

	/**
	 * GET /facebook-oauth/callback?code=AUTH_CODE&state=ENCODED_STATE
	 * or  /facebook-oauth/callback?error=access_denied&error_description=...&state=...
	 */
	@GetMapping("/callback")
	public ResponseEntity<String> callback(
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "error_description", required = false) String errorDescription) {

		if (error != null) {
			if (state != null) {
				try {
					String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
					String clientId = decoded.split(":", 2)[0];
					onboardingService.markPlatformFailed(clientId, "META");
				} catch (Exception ignored) {
				}
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Facebook authorization denied: " + (errorDescription != null ? errorDescription : error));
		}

		if (code == null || state == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Missing required parameters: code and state");
		}

		try {
			facebookOAuthService.handleOAuthCallback(code, state);
			return ResponseEntity.status(HttpStatus.FOUND)
					.location(URI.create(facebookConUrl + "?facebook=success")).build();
		} catch (Exception e) {
			try {
				String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
				String clientId = decoded.split(":", 2)[0];
				onboardingService.markPlatformFailed(clientId, "META");
			} catch (Exception ignored) {
			}
			return ResponseEntity.status(HttpStatus.FOUND)
					.location(URI.create(facebookConUrl + "?gmb=error&message=Failed+to+connect+GMB"))
					.build();
		}
	}

	/**z 
	 * GET /facebook-oauth/resync-pages?client_id=CLIENT_ID&user_id=USER_ID
	 */
	@GetMapping("/resync-pages")
	public ResponseEntity<String> resyncPages(@RequestParam("client_id") String clientId,
			@RequestParam("user_id") String userId) {
		try {
			facebookOAuthService.fetchAndSavePageAccessTokens(clientId, userId);
			return ResponseEntity.ok("Facebook pages re-synced successfully for clientId=" + clientId);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to re-sync Facebook pages: " + e.getMessage());
		}
	}
}

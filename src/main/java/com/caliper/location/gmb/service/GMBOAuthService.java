package com.caliper.location.gmb.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.caliper.location.config.entity.OAuthAppConfig;
import com.caliper.location.config.repository.OAuthAppConfigRepository;
import com.caliper.location.gmb.dto.response.GMBAccountResponse;
import com.caliper.location.gmb.dto.response.GMBLocationResponse;
import com.caliper.location.gmb.dto.response.GMBOperationHoursResponse;
import com.caliper.location.gmb.entity.GMBAccount;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.entity.GMBOAuthToken;
import com.caliper.location.gmb.entity.GMBOperationHours;
import com.caliper.location.gmb.repository.GMBAccountRepository;
import com.caliper.location.gmb.repository.GMBOAuthTokenRepository;
import com.caliper.onboarding.service.OnboardingService;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.mybusinessaccountmanagement.v1.MyBusinessAccountManagement;
import com.google.api.services.mybusinessaccountmanagement.v1.model.Account;
import com.google.api.services.mybusinessaccountmanagement.v1.model.ListAccountsResponse;

@Service
public class GMBOAuthService {

	private static final Logger log = LoggerFactory.getLogger(GMBOAuthService.class);
	private static final String GMB_SCOPE = "https://www.googleapis.com/auth/business.manage";
	private static final HttpTransport HTTP_TRANSPORT = Utils.getDefaultTransport();
	private static final JsonFactory JSON_FACTORY = Utils.getDefaultJsonFactory();

	@Value("${gmb.oauth.redirect-uri}")
	private String redirectUri;

	@Autowired
	private OAuthAppConfigRepository oAuthAppConfigRepository;

	@Autowired
	private GMBOAuthTokenRepository gmbOAuthTokenRepository;

	@Autowired
	private GMBAccountRepository gmbAccountRepository;

	@Autowired
	private GMBSessionFactory gmbOAuthSessionFactory;

	@Autowired
	private OnboardingService onboardingService;

	@Autowired
	private GMBLocationService gmbLocationService;

	@Autowired
	@Qualifier("gmbImportPreviewExecutor")
	private Executor gmbImportPreviewExecutor;

	private OAuthAppConfig getGmbConfig() {
		return oAuthAppConfigRepository.findById("GMB")
				.orElseThrow(() -> new RuntimeException("GMB OAuth config not found in database"));
	}

	public String generateAuthorizationUrl(String clientId, String userId) {
		OAuthAppConfig config = getGmbConfig();
		String state = Base64.getUrlEncoder().withoutPadding()
				.encodeToString((clientId + ":" + userId).getBytes(StandardCharsets.UTF_8));

		return new GoogleAuthorizationCodeRequestUrl(
				config.getAppId(),
				redirectUri,
				Collections.singleton(GMB_SCOPE))
				.setAccessType("offline")
				.setApprovalPrompt("force")
				.setState(state)
				.build();
	}

	public void handleOAuthCallback(String code, String state) throws IOException {
        OAuthAppConfig config = getGmbConfig();
        String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
        String[] parts = decoded.split(":", 2);
        String clientId = parts[0];
        String userId = (parts.length > 1) ? parts[1] : "system";

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                HTTP_TRANSPORT, JSON_FACTORY,
                config.getAppId(), config.getAppSecret(),
                code, redirectUri)
                .execute();

        Date tokenExpiry = null;
        if (tokenResponse.getExpiresInSeconds() != null) {
            tokenExpiry = new Date(System.currentTimeMillis() + tokenResponse.getExpiresInSeconds() * 1000L);
        }

        GMBOAuthToken existing = gmbOAuthTokenRepository.findByClientId(clientId);
        if (existing != null) {
            existing.setGcpClientId(config.getAppId());
            existing.setGcpClientSecret(config.getAppSecret());
            existing.setAccessToken(tokenResponse.getAccessToken());
            if (tokenResponse.getRefreshToken() != null) {
                existing.setRefreshToken(tokenResponse.getRefreshToken());
            }
            existing.setTokenExpiry(tokenExpiry);
            existing.setModifiedDate(new Date());
            gmbOAuthTokenRepository.save(existing);
        } else {
            gmbOAuthTokenRepository.save(GMBOAuthToken.builder()
                    .clientId(clientId)
                    .gcpClientId(config.getAppId())
                    .gcpClientSecret(config.getAppSecret())
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .tokenExpiry(tokenExpiry)
                    .modifiedDate(new Date())
                    .build());
        }

        log.info("GMBOAuthService :: handleOAuthCallback :: token stored for clientId={}", clientId);

        fetchAndSaveGMBAccounts(clientId, userId);

//        GMBAccount gmbAccount = gmbAccountRepository.findTopByClientIdOrderByLastModifiedDateDesc(clientId);
//        try {
//            gmbLocationService.fetchAndProcessLocationsSync(clientId, gmbAccount, false, true, true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        onboardingService.markGmbConnected(clientId);
    }

	public List<GMBAccountResponse> fetchGMBAccounts(String clientId, String userId) {
	    List<GMBAccountResponse> accountResponses = new ArrayList<>();

	    try {
	        MyBusinessAccountManagement accountManagement = gmbOAuthSessionFactory.getGMBAccountSession(clientId);
	        List<Account> accountList = new ArrayList<>();
	        String nextPageToken = null;

	        do {
	            MyBusinessAccountManagement.Accounts.List request = accountManagement.accounts().list();
	            request.setPageSize(100); // Optional (default is 20)
	            if (nextPageToken != null && !nextPageToken.isEmpty()) {
	                request.setPageToken(nextPageToken);
	            }
	            ListAccountsResponse response = request.execute();
	            if (response.getAccounts() != null) {
	                accountList.addAll(response.getAccounts());
	            }
	            nextPageToken = response.getNextPageToken();
	        } while (nextPageToken != null && !nextPageToken.isEmpty());

	        List<GMBAccount> gmbAccounts = accountList.stream()
	        	    .map(account -> {
	        	        GMBAccount gmbAccount = new GMBAccount();
	        	        gmbAccount.setAccountId(account.getName());
	        	        gmbAccount.setAccountName(account.getAccountName());
	        	        gmbAccount.setClientId(clientId);
	        	        return gmbAccount;
	        	    })
	        	    .collect(Collectors.toList());

	        List<CompletableFuture<GMBAccountResponse>> futures = gmbAccounts.stream()
	        	    .map(gmbAccount -> CompletableFuture.supplyAsync(
	        	            () -> {
	        	                List<GMBLocation> newLocations = gmbLocationService.fetchNewLocationsForAccount(gmbAccount);
	        	                return GMBAccountResponse.builder()
	        	                        .accountId(gmbAccount.getAccountId())
	        	                        .accountName(gmbAccount.getAccountName())
	        	                        .clientId(clientId)
	        	                        .userId(userId)
	        	                        .locations(newLocations.stream()
	        	                                .map(location -> mapToLocationResponse(location, clientId))
	        	                                .collect(Collectors.toList()))
	        	                        .build();
	        	            },
	        	            gmbImportPreviewExecutor))
	        	    .collect(Collectors.toList());

			for (CompletableFuture<GMBAccountResponse> future : futures) {
				accountResponses.add(future.join());
			}

	    } catch (Exception e) {
	        log.error("GMBOAuthService :: fetchGMBAccounts :: failed for clientId={} : {}",
	                clientId, e.getMessage(), e);
	    }
	    return accountResponses;
	}

	private GMBLocationResponse mapToLocationResponse(GMBLocation location, String clientId) {
		return GMBLocationResponse.builder()
				.gmbLocationId(location.getGmbLocationId())
				.accountId(location.getAccountId())
				.clientId(clientId)
				.dealerId(location.getDealerId())
				.name(location.getName())
				.address(location.getAddress())
				.area(location.getArea())
				.city(location.getCity())
				.state(location.getState())
				.pincode(location.getPincode())
				.latitude(location.getLatitude())
				.longitude(location.getLongitude())
				.countryCode(location.getCountryCode())
				.languageCode(location.getLanguageCode())
				.phoneNumber(location.getPhoneNumber())
				.additionalPhones(location.getAdditionalPhones())
				.mapUrl(location.getMapUrl())
				.newReviewUrl(location.getNewReviewUrl())
				.openInfoCanReopen(Boolean.TRUE.equals(location.getOpenInfoCanReopen()))
				.openInfoStatus(location.getOpenInfoStatus())
				.websiteUrl(location.getWebsiteUrl())
				.description(location.getDescription())
				.primaryCategory(location.getPrimaryCategory())
				.additionalCategories(location.getAdditionalCategories())
				.labels(location.getLabels())
				.status(location.getStatus())
				.placeActionId(location.getPlaceActionId())
				.appointmentLink(location.getAppointmentLink())
				.address1(location.getAddress1())
				.address2(location.getAddress2())
				.address3(location.getAddress3())
				.insertedDate(location.getInsertedDate())
				.whatsappUrl(location.getWhatsappUrl())
				.instagramUrl(location.getInstagramUrl())
				.youtubeUrl(location.getYoutubeUrl())
				.facebookUrl(location.getFacebookUrl())
				.twitterUrl(location.getTwitterUrl())
				.linkedinUrl(location.getLinkedinUrl())
				.gmbOperationHours(mapToOperationHoursResponse(location.getGmbOperationHours()))
				.build();
	}

	private GMBOperationHoursResponse mapToOperationHoursResponse(GMBOperationHours hours) {
		if (hours == null) {
			return null;
		}
		return GMBOperationHoursResponse.builder()
				.mondayOpenTime(hours.getMondayOpenTime())
				.mondayCloseTime(hours.getMondayCloseTime())
				.tuesdayOpenTime(hours.getTuesdayOpenTime())
				.tuesdayCloseTime(hours.getTuesdayCloseTime())
				.wednesdayOpenTime(hours.getWednesdayOpenTime())
				.wednesdayCloseTime(hours.getWednesdayCloseTime())
				.thursdayOpenTime(hours.getThursdayOpenTime())
				.thursdayCloseTime(hours.getThursdayCloseTime())
				.fridayOpenTime(hours.getFridayOpenTime())
				.fridayCloseTime(hours.getFridayCloseTime())
				.saturdayOpenTime(hours.getSaturdayOpenTime())
				.saturdayCloseTime(hours.getSaturdayCloseTime())
				.sundayOpenTime(hours.getSundayOpenTime())
				.sundayCloseTime(hours.getSundayCloseTime())
				.build();
	}

	public void fetchAndSaveGMBAccounts(String clientId, String userId) {
		try {
			MyBusinessAccountManagement accountManagement =
					gmbOAuthSessionFactory.getGMBAccountSession(clientId);

			String nextPageToken = null;
			do {
				MyBusinessAccountManagement.Accounts.List request = accountManagement.accounts().list();
				if (nextPageToken != null) {
					request.setPageToken(nextPageToken);
				}
				ListAccountsResponse response = request.execute();
				List<Account> accounts = response.getAccounts();

				if (accounts != null) {
					for (Account account : accounts) {
						GMBAccount existingAccount = gmbAccountRepository
								.findByClientIdAndAccountName(clientId, account.getName())
								.orElse(null);
						if (existingAccount != null) {
							existingAccount.setAccountName(account.getAccountName());
							existingAccount.setLastModifiedBy(userId);
							existingAccount.setLastModifiedDate(new Date());
							gmbAccountRepository.save(existingAccount);
						} else {
							gmbAccountRepository.save(GMBAccount.builder()
									.accountId(account.getName())
									.accountName(account.getAccountName())
									.clientId(clientId)
									.lastModifiedBy(userId)
									.lastModifiedDate(new Date())
									.status("Not Selected")
									.build());
						}
						log.info("GMBOAuthService :: fetchAndSaveGMBAccounts :: saved accountId={} clientId={}",
								account.getName(), clientId);
					}
				}
				nextPageToken = response.getNextPageToken();
			} while (nextPageToken != null);

		} catch (Exception e) {
			log.error("GMBOAuthService :: fetchAndSaveGMBAccounts :: failed for clientId={} : {}",
					clientId, e.getMessage(), e);
		}
	}
}

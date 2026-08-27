package com.caliper.location.facebook.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.caliper.location.config.entity.OAuthAppConfig;
import com.caliper.location.config.repository.OAuthAppConfigRepository;
import com.caliper.location.facebook.entity.FacebookPage;
import com.caliper.location.facebook.repository.FacebookPageRepository;
import com.caliper.onboarding.service.OnboardingService;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.Page;

@Service
public class FacebookOAuthService {

    private static final Logger log = LoggerFactory.getLogger(FacebookOAuthService.class);

    private static final String FB_SCOPE =
            "pages_show_list,pages_read_engagement,pages_manage_posts,pages_manage_metadata";

    @Value("${facebook.oauth.redirect-uri}")
    private String redirectUri;

    @Autowired
    private OAuthAppConfigRepository oAuthAppConfigRepository;

    @Autowired
    private FacebookPageRepository facebookPageRepository;

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private FacebookLocationService facebookLocationService;

    private OAuthAppConfig getFacebookConfig() {
        return oAuthAppConfigRepository.findById("FACEBOOK")
                .orElseThrow(() -> new RuntimeException("Facebook OAuth config not found in database"));
    }

    public String generateAuthorizationUrl(String clientId, String userId) {
        OAuthAppConfig config = getFacebookConfig();
        String state = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((clientId + ":" + userId).getBytes(StandardCharsets.UTF_8));

        return UriComponentsBuilder
                .fromHttpUrl("https://www.facebook.com/dialog/oauth")
                .queryParam("client_id", config.getAppId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", FB_SCOPE)
                .queryParam("state", state)
                .queryParam("response_type", "code")
                .toUriString();
    }

    public void handleOAuthCallback(String code, String state) {
        OAuthAppConfig config = getFacebookConfig();
        String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
        String[] parts = decoded.split(":", 2);
        String clientId = parts[0];
        String userId = (parts.length > 1) ? parts[1] : "system";

        DefaultFacebookClient client = new DefaultFacebookClient(Version.LATEST);

        AccessToken shortLivedToken = client.obtainUserAccessToken(config.getAppId(), config.getAppSecret(), redirectUri, code);
        log.info("FacebookOAuthService :: handleOAuthCallback :: short-lived token obtained for clientId={}", clientId);

        AccessToken longLivedToken = client.obtainExtendedAccessToken(config.getAppId(), config.getAppSecret(), shortLivedToken.getAccessToken());
        log.info("FacebookOAuthService :: handleOAuthCallback :: long-lived token obtained for clientId={}", clientId);

        fetchAndSavePageAccessTokens(clientId, userId, longLivedToken.getAccessToken());

        List<FacebookPage> facebookPages = facebookPageRepository.findByClientId(clientId);
        try {
            for (FacebookPage facebookPageAccount : facebookPages) {
                facebookLocationService.fetchAndProcessLocationsSync(clientId, facebookPageAccount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        onboardingService.markMetaConnected(clientId);
    }

    public void fetchAndSavePageAccessTokens(String clientId, String userId) {
        List<FacebookPage> existing = facebookPageRepository.findByClientId(clientId);
        if (existing.isEmpty()) {
            throw new IllegalStateException("No Facebook token found for clientId=" + clientId);
        }
        fetchAndSavePageAccessTokens(clientId, userId, existing.get(0).getUserAccessToken());
    }

    public void fetchAndSavePageAccessTokens(String clientId, String userId, String userAccessToken) {
        OAuthAppConfig config = getFacebookConfig();
        try {
            FacebookClient userClient = new DefaultFacebookClient(userAccessToken, config.getAppSecret(), Version.LATEST);

            List<FacebookPage> existingPages = facebookPageRepository.findByClientId(clientId);
            Map<Long, FacebookPage> existingByPageId = existingPages.stream()
                    .collect(Collectors.toMap(FacebookPage::getFacebookPageId, p -> p));

            String afterCursor = null;
            do {
                Connection<Page> accounts;
                if (afterCursor == null) {
                    accounts = userClient.fetchConnection("me/accounts", Page.class,
                            Parameter.with("fields", "id,name,access_token,category"));
                } else {
                    accounts = userClient.fetchConnection("me/accounts", Page.class,
                            Parameter.with("fields", "id,name,access_token,category"),
                            Parameter.with("after", afterCursor));
                }

                for (Page account : accounts.getData()) {
                    Long pageId = Long.parseLong(account.getId());
                    FacebookPage page = existingByPageId.getOrDefault(pageId, new FacebookPage());

                    page.setFacebookPageId(pageId);
                    page.setClientId(clientId);
                    page.setAppClientId(Long.parseLong(config.getAppId()));
                    page.setAppClientSecret(config.getAppSecret());
                    page.setUserAccessToken(userAccessToken);
                    page.setPageAccessToken(account.getAccessToken());
                    page.setPageName(account.getName());
                    page.setLastModifiedBy(userId);
                    page.setLastModifiedDate(new Date());

                    facebookPageRepository.save(page);
                    log.info("FacebookOAuthService :: fetchAndSavePageAccessTokens :: saved pageId={} clientId={}",
                            pageId, clientId);
                }

                afterCursor = accounts.getAfterCursor();
            } while (afterCursor != null);

        } catch (Exception e) {
            log.error("FacebookOAuthService :: fetchAndSavePageAccessTokens :: failed for clientId={} : {}",
                    clientId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch Facebook page tokens for clientId=" + clientId, e);
        }
    }
}

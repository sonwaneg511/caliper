package com.caliper.campaign.facebook.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.caliper.campaign.facebook.entity.MetaCampaign;
import com.caliper.campaign.facebook.entity.MetaLead;
import com.caliper.campaign.facebook.repository.MetaCampaignRepository;
import com.caliper.campaign.facebook.repository.MetaLeadRepository;
import com.caliper.location.facebook.entity.FacebookAccount;
import com.caliper.location.facebook.repository.FacebookAccountRepository;
import com.caliper.metaads.service.MetaAdsApiService;
import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.Lead;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MetaWebhookService {

    @Value("${meta.webhook.verify.token:caliper_meta_verify_token}")
    private String verifyToken;

    @Autowired
    private MetaLeadRepository metaLeadRepository;

    @Autowired
    private MetaCampaignRepository metaCampaignRepository;

    @Autowired
    private FacebookAccountRepository facebookAccountRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String verifyWebhook(String mode, String token, String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Meta webhook verified successfully");
            return challenge;
        }
        throw new SecurityException("Meta webhook verification failed: token mismatch");
    }

    public void processLeadEvent(String payload, String signature) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String object = root.path("object").asText();

            if (!"page".equalsIgnoreCase(object)) {
                log.warn("Received non-page webhook event: {}", object);
                return;
            }

            JsonNode entries = root.path("entry");
            for (JsonNode entry : entries) {
                JsonNode changes = entry.path("changes");
                for (JsonNode change : changes) {
                    String field = change.path("field").asText();
                    if ("leadgen".equalsIgnoreCase(field)) {
                        processLeadChange(change.path("value"));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing Meta webhook payload: {}", e.getMessage(), e);
        }
    }

    private void processLeadChange(JsonNode value) {
        String leadId = value.path("leadgen_id").asText();
        String adId = value.path("ad_id").asText();
        String formId = value.path("form_id").asText();
        String metaCampaignId = value.path("campaign_id").asText();
        long createdTimeEpoch = value.path("created_time").asLong(0L);

        if (metaLeadRepository.findByMetaLeadId(leadId).isPresent()) {
            log.info("Lead already stored, skipping: {}", leadId);
            return;
        }

        // Resolve client/dealer from our campaign record
        String resolvedClientId = "";
        String resolvedDealerId = "";
        MetaCampaign campaign = metaCampaignRepository.findAll().stream()
                .filter(c -> metaCampaignId.equals(c.getMetaCampaignId()))
                .findFirst().orElse(null);
        if (campaign != null) {
            resolvedClientId = campaign.getClientId();
            resolvedDealerId = campaign.getDealerId();
        }

        String leadDataJson = "{}";
        final String clientIdFinal = resolvedClientId;
        final String dealerIdFinal = resolvedDealerId;

        try {
            FacebookAccount account = clientIdFinal.isBlank()
                    ? facebookAccountRepository.findAll().stream().findFirst().orElse(null)
                    : facebookAccountRepository
                            .findByClientIdAndDealerId(clientIdFinal, dealerIdFinal)
                            .or(() -> facebookAccountRepository.findByClientIdAndDealerIdIsNull(clientIdFinal))
                            .orElse(null);

            if (account != null) {
                APIContext ctx = MetaAdsApiService.getApiContext(account.getAccessToken());
                Lead lead = MetaAdsApiService.fetchLead(ctx, leadId);
                leadDataJson = objectMapper.writeValueAsString(lead.getFieldFieldData());
            }

        } catch (Exception e) {
            log.warn("Could not fetch full lead details for leadId={}: {}", leadId, e.getMessage());
            leadDataJson = value.toString();
        }

        Date createdTime = createdTimeEpoch > 0
                ? new Date(createdTimeEpoch * 1000L)
                : new Date();

        MetaLead metaLead = MetaLead.builder()
                .metaLeadId(leadId)
                .metaAdId(adId)
                .formId(formId)
                .metaCampaignId(metaCampaignId)
                .clientId(resolvedClientId)
                .dealerId(resolvedDealerId)
                .leadData(leadDataJson)
                .createdTime(createdTime)
                .receivedAt(new Date())
                .build();

        metaLeadRepository.save(metaLead);
        log.info("Meta lead saved: leadId={}, campaignId={}", leadId, metaCampaignId);
    }

    public boolean verifySignature(String payload, String signature, String appSecret) {
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }
        try {
            String expectedHash = signature.substring("sha256=".length());
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedHash = bytesToHex(rawHash);
            return computedHash.equalsIgnoreCase(expectedHash);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

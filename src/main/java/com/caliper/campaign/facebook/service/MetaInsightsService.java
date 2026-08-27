package com.caliper.campaign.facebook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.campaign.facebook.dto.response.MetaCampaignReportResponse;
import com.caliper.campaign.facebook.entity.MetaCampaign;
import com.caliper.campaign.facebook.repository.MetaCampaignRepository;
import com.caliper.location.facebook.entity.FacebookAccount;
import com.caliper.location.facebook.repository.FacebookAccountRepository;
import com.caliper.metaads.service.MetaAdsApiService;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.APINodeList;
import com.facebook.ads.sdk.AdsInsights;

@Service
public class MetaInsightsService {

    @Autowired
    private MetaCampaignRepository metaCampaignRepository;

    @Autowired
    private FacebookAccountRepository facebookAccountRepository;

    public MetaCampaignReportResponse getCampaignInsights(Long campaignId, String datePreset) {
        MetaCampaign campaign = metaCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta campaign not found: " + campaignId));

        if ("-1".equals(campaign.getMetaCampaignId())) {
            throw new InvalidRequestException("Campaign has not been deployed yet: " + campaignId);
        }

        FacebookAccount account = facebookAccountRepository
                .findByClientIdAndDealerId(campaign.getClientId(), campaign.getDealerId())
                .or(() -> facebookAccountRepository.findByClientIdAndDealerIdIsNull(campaign.getClientId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facebook account not found for client: " + campaign.getClientId()));

        try {
            APIContext ctx = MetaAdsApiService.getApiContext(account.getAccessToken());
            String preset = (datePreset != null && !datePreset.isBlank()) ? datePreset : "LAST_30_DAYS";

            APINodeList<AdsInsights> insightsList = MetaAdsApiService.getCampaignInsights(
                    ctx, campaign.getMetaCampaignId(), preset, MetaAdsApiService.defaultInsightFields());

            MetaCampaignReportResponse.MetaCampaignReportResponseBuilder builder = MetaCampaignReportResponse.builder()
                    .campaignId(campaign.getId())
                    .metaCampaignId(campaign.getMetaCampaignId())
                    .campaignName(campaign.getCampaignName())
                    .datePreset(preset);

            if (insightsList != null && !insightsList.isEmpty()) {
                AdsInsights insight = insightsList.get(0);
                builder.impressions(insight.getFieldImpressions())
                        .reach(insight.getFieldReach())
                        .clicks(insight.getFieldClicks())
                        .spend(insight.getFieldSpend());

                String leads = extractLeadsFromActions(insight);
                builder.leads(leads);
            }

            return builder.build();

        } catch (Exception e) {
            throw new InvalidRequestException("Failed to fetch Meta insights: " + e.getMessage());
        }
    }

    private String extractLeadsFromActions(AdsInsights insight) {
        try {
            if (insight.getFieldActions() == null) return "0";
            return insight.getFieldActions().stream()
                    .filter(a -> "lead".equalsIgnoreCase(a.getFieldActionType())
                            || "onsite_conversion.lead_grouped".equalsIgnoreCase(a.getFieldActionType()))
                    .map(a -> a.getFieldValue())
                    .findFirst()
                    .orElse("0");
        } catch (Exception e) {
            return "0";
        }
    }
}

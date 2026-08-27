package com.caliper.location.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CampaignSettingsBody {
    private Double radius;
    private String radiusUnit;
    private String campaignPhoneNumber;
    private String callAdsPhoneNumber;
    private String landingPageUrl;
    private String youtubeUrl;
}

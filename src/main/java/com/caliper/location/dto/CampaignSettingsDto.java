package com.caliper.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CampaignSettingsDto {
    private String industry;
    private String subIndustry;
    private Double radius;
    private String radiusUnit;
    private String clientEmail;
    private String campaignPhoneNumber;
    private String callAdsPhoneNumber;
    private String landingPageUrl;
    private String youtubeUrl;
    private String platform;
    private String objective;

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getSubIndustry() {
        return subIndustry;
    }

    public void setSubIndustry(String subIndustry) {
        this.subIndustry = subIndustry;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public String getRadiusUnit() {
        return radiusUnit;
    }

    public void setRadiusUnit(String radiusUnit) {
        this.radiusUnit = radiusUnit;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getCampaignPhoneNumber() {
        return campaignPhoneNumber;
    }

    public void setCampaignPhoneNumber(String campaignPhoneNumber) {
        this.campaignPhoneNumber = campaignPhoneNumber;
    }

    public String getCallAdsPhoneNumber() {
        return callAdsPhoneNumber;
    }

    public void setCallAdsPhoneNumber(String callAdsPhoneNumber) {
        this.callAdsPhoneNumber = callAdsPhoneNumber;
    }

    public String getLandingPageUrl() {
        return landingPageUrl;
    }

    public void setLandingPageUrl(String landingPageUrl) {
        this.landingPageUrl = landingPageUrl;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }
}

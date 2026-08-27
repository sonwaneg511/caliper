package com.caliper.location.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LocationDetailsDto {
    private String dealerName;
    private String dealerId;
    private String clientName;

    private LocationOverviewDto locationOverview;
    private GmbDetailsDto gmbDetails;
    private FacebookDetailsDto facebookDetails;
    private CampaignSettingsDto campaignSettings;
    private Map<String, Object> media;
    private int healthScore;

    // Getters and Setters

    public String getDealerName() {
        return dealerName;
    }

    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
    }

    public String getDealerId() {
        return dealerId;
    }

    public void setDealerId(String dealerId) {
        this.dealerId = dealerId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public LocationOverviewDto getLocationOverview() {
        return locationOverview;
    }

    public void setLocationOverview(LocationOverviewDto locationOverview) {
        this.locationOverview = locationOverview;
    }

    public GmbDetailsDto getGmbDetails() {
        return gmbDetails;
    }

    public void setGmbDetails(GmbDetailsDto gmbDetails) {
        this.gmbDetails = gmbDetails;
    }

    public FacebookDetailsDto getFacebookDetails() {
        return facebookDetails;
    }

    public void setFacebookDetails(FacebookDetailsDto facebookDetails) {
        this.facebookDetails = facebookDetails;
    }

    public CampaignSettingsDto getCampaignSettings() {
        return campaignSettings;
    }

    public void setCampaignSettings(CampaignSettingsDto campaignSettings) {
        this.campaignSettings = campaignSettings;
    }

    public Map<String, Object> getMedia() {
        return media;
    }

    public void setMedia(Map<String, Object> media) {
        this.media = media;
    }
}

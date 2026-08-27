package com.caliper.location.dto;

import java.util.List;

import com.caliper.location.dto.response.CategoryDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GmbDetailsDto {
    private String businessName;
    private String mapUrl;
    private String reviewUrl;
    private String websiteUrl;
    private String phoneNumber;
    private CategoryDetails gmbPrimaryCategoryDetails;
    private List<CategoryDetails> gmbAdditionalCategoryDetails;
    private String status;
    private String openInfoStatus;
    private String languageCode;
    private String appointmentLink;
    private String whatsappUrl;
    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String linkedinUrl;
    private String youtubeUrl;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public String getReviewUrl() {
        return reviewUrl;
    }

    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

   

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOpenInfoStatus() {
        return openInfoStatus;
    }

    public void setOpenInfoStatus(String openInfoStatus) {
        this.openInfoStatus = openInfoStatus;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getAppointmentLink() {
        return appointmentLink;
    }

    public void setAppointmentLink(String appointmentLink) {
        this.appointmentLink = appointmentLink;
    }

    public String getWhatsappUrl() {
        return whatsappUrl;
    }

    public void setWhatsappUrl(String whatsappUrl) {
        this.whatsappUrl = whatsappUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getTwitterUrl() {
        return twitterUrl;
    }

    public void setTwitterUrl(String twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }
}
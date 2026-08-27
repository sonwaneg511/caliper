package com.caliper.location.gmb.service;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.images.entity.LocationImage;
import com.caliper.images.repository.LocationImageMapRepository;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.entity.GMBOperationHours;

/**
 * GBP (Google Business Profile) completeness scoring — 100-point rubric used by the
 * GMB Location Report, the dashboard Locations audit score, and /api/user/self.
 */
@Component
public class GBPCompletenessScoreCalculator {

    @Autowired
    private LocationImageMapRepository locationImageMapRepository;

    // ---------------------------------------------------------------
    // Points per parameter (total = 100)
    // ---------------------------------------------------------------
    private static final double POINTS_STORE_PHONE_NUMBER = 6;
    private static final double POINTS_ADDRESS = 6;
    private static final double POINTS_HOURS_OF_OPERATION = 6;
    private static final double POINTS_PRIMARY_CATEGORY = 6;
    private static final double POINTS_CITY = 6;

    private static final double POINTS_AREA = 5;
    private static final double POINTS_STATE = 5;
    private static final double POINTS_COUNTRY = 5;
    private static final double POINTS_VALID_PINCODE = 5;
    private static final double POINTS_WEBSITE_URL = 5;
    private static final double POINTS_BUSINESS_DESCRIPTION = 5;
    private static final double POINTS_APPOINTMENT_LINK = 5;
    private static final double POINTS_SOCIAL_MEDIA_LINKS = 5;
    private static final double POINTS_ADDITIONAL_PHONE_NUMBER = 5;
    private static final double POINTS_ADDITIONAL_CATEGORIES = 5;
    private static final double POINTS_LOGO = 5;
    private static final double POINTS_COVER_IMAGE = 5;
    private static final double POINTS_INTERIOR_IMAGES = 5;
    private static final double POINTS_EXTERIOR_IMAGES = 5;

    private static final double TOTAL_POINTS =
            POINTS_STORE_PHONE_NUMBER + POINTS_ADDRESS + POINTS_HOURS_OF_OPERATION
                    + POINTS_PRIMARY_CATEGORY + POINTS_CITY + POINTS_AREA + POINTS_STATE
                    + POINTS_COUNTRY + POINTS_VALID_PINCODE + POINTS_WEBSITE_URL
                    + POINTS_BUSINESS_DESCRIPTION + POINTS_APPOINTMENT_LINK
                    + POINTS_SOCIAL_MEDIA_LINKS + POINTS_ADDITIONAL_PHONE_NUMBER
                    + POINTS_ADDITIONAL_CATEGORIES + POINTS_LOGO + POINTS_COVER_IMAGE
                    + POINTS_INTERIOR_IMAGES + POINTS_EXTERIOR_IMAGES;

    // Expected postal code digit length for countries with purely numeric postal codes.
    // Pincode is stored as a Long, so alphanumeric formats (UK, Canada, ...) can never be
    // represented here — those country codes fall back to a presence-only check below.
    private static final Map<String, Integer> PINCODE_DIGIT_LENGTH_BY_COUNTRY = Map.ofEntries(
            Map.entry("IN", 6),
            Map.entry("US", 5),
            Map.entry("AU", 4),
            Map.entry("SG", 6),
            Map.entry("DE", 5),
            Map.entry("FR", 5),
            Map.entry("ES", 5),
            Map.entry("IT", 5)
    );

    /**
     * GBP completeness score = percentage (0-100) of the 100 available points present
     * on the location. See POINTS_* constants for the per-parameter weights.
     */
    public int calculateScore(GMBLocation location) {

        double score = 0;

        if (isNonBlank(location.getPhoneNumber())) score += POINTS_STORE_PHONE_NUMBER;
        if (isNonBlank(location.getAddress1())) score += POINTS_ADDRESS;
        score += operationHoursScore(location.getGmbOperationHours());
        if (isValidPrimaryCategory(location.getPrimaryCategory())) score += POINTS_PRIMARY_CATEGORY;
        if (isNonBlank(location.getCity())) score += POINTS_CITY;

        if (isNonBlank(location.getArea())) score += POINTS_AREA;
        if (isNonBlank(location.getState())) score += POINTS_STATE;
        if (isNonBlank(location.getCountryCode())) score += POINTS_COUNTRY;
        if (isValidPincode(location.getPincode(), location.getCountryCode())) score += POINTS_VALID_PINCODE;
        if (isNonBlank(location.getWebsiteUrl())) score += POINTS_WEBSITE_URL;
        if (isNonBlank(location.getDescription())) score += POINTS_BUSINESS_DESCRIPTION;
        if (isNonBlank(location.getAppointmentLink())) score += POINTS_APPOINTMENT_LINK;
        if (hasAnySocialMediaLink(location)) score += POINTS_SOCIAL_MEDIA_LINKS;
        if (isNonBlank(location.getAdditionalPhones())) score += POINTS_ADDITIONAL_PHONE_NUMBER;
        if (isNonBlank(location.getAdditionalCategories())) score += POINTS_ADDITIONAL_CATEGORIES;

        String dealerId = location.getDealerId();
        if (hasImageInCategory(dealerId, LocationImage.CATEGORY_LOGO)) score += POINTS_LOGO;
        if (hasImageInCategory(dealerId, LocationImage.CATEGORY_COVER)) score += POINTS_COVER_IMAGE;
        if (hasImageInCategory(dealerId, LocationImage.CATEGORY_INTERIOR)) score += POINTS_INTERIOR_IMAGES;
        if (hasImageInCategory(dealerId, LocationImage.CATEGORY_EXTERIOR)) score += POINTS_EXTERIOR_IMAGES;

        return (int) Math.round((score / TOTAL_POINTS) * 100);
    }

    private boolean isNonBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isValidPrimaryCategory(Long primaryCategory) {
        return primaryCategory != null && primaryCategory > 0;
    }

    private boolean hasAnySocialMediaLink(GMBLocation location) {
        return isNonBlank(location.getWhatsappUrl())
                || isNonBlank(location.getInstagramUrl())
                || isNonBlank(location.getYoutubeUrl())
                || isNonBlank(location.getFacebookUrl())
                || isNonBlank(location.getTwitterUrl())
                || isNonBlank(location.getLinkedinUrl());
    }

    private boolean hasImageInCategory(String dealerId, String category) {
        if (dealerId == null) {
            return false;
        }
        return !locationImageMapRepository.findAllByDealerIdAndImageCategory(dealerId, category).isEmpty();
    }

    private boolean isValidPincode(Long pincode, String countryCode) {

        if (pincode == null || pincode <= 0) {
            return false;
        }

        Integer expectedDigitLength = PINCODE_DIGIT_LENGTH_BY_COUNTRY.get(
                countryCode != null ? countryCode.trim().toUpperCase() : null);

        // Country not in the numeric-postal-code table: fall back to presence-only check.
        return expectedDigitLength == null
                || String.valueOf(pincode).length() == expectedDigitLength;
    }

    /**
     * Proportional credit: POINTS_HOURS_OF_OPERATION split across the 7 days,
     * awarded per day that has both an open and close time set.
     */
    private double operationHoursScore(GMBOperationHours hours) {

        if (hours == null) {
            return 0;
        }

        String[][] dayPairs = {
                {hours.getMondayOpenTime(), hours.getMondayCloseTime()},
                {hours.getTuesdayOpenTime(), hours.getTuesdayCloseTime()},
                {hours.getWednesdayOpenTime(), hours.getWednesdayCloseTime()},
                {hours.getThursdayOpenTime(), hours.getThursdayCloseTime()},
                {hours.getFridayOpenTime(), hours.getFridayCloseTime()},
                {hours.getSaturdayOpenTime(), hours.getSaturdayCloseTime()},
                {hours.getSundayOpenTime(), hours.getSundayCloseTime()}
        };

        long daysFilled = Arrays.stream(dayPairs)
                .filter(pair -> isNonBlank(pair[0]) && isNonBlank(pair[1]))
                .count();

        return (daysFilled / 7.0) * POINTS_HOURS_OF_OPERATION;
    }
}

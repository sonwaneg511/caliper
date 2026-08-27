package com.caliper.task;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.campaign.facebook.entity.MetaGeoLocation;
import com.caliper.campaign.facebook.repository.MetaGeoLocationRepository;
import com.caliper.location.facebook.entity.FacebookAccount;
import com.caliper.location.facebook.repository.FacebookAccountRepository;
import com.caliper.metaads.service.MetaAdsApiService;

/**
 * Fetches geo-location keys from the Meta Targeting Search API and stores them
 * in the meta_geo_location table so they can be looked up during campaign creation
 * without making live Meta API calls at that point.
 *
 * XML parameters:
 *   <client_id>      — required: client whose Facebook access token is used for API calls
 *   <location_type>  — optional: "city" | "region" | "country" | "both" (default: both)
 *   <country_code>   — optional: ISO country code to filter results (default: IN)
 *   <queries>        — optional: comma-separated search terms. Falls back to built-in lists.
 *
 * Example XML:
 *
 * <parameters>
 *     <client_id>1</client_id>
 *     <location_type>country</location_type>
 *     <queries>India,United Arab Emirates</queries>
 * </parameters>
 */
@Service
public class MetaGeoLocationSyncTask extends ParameterizedJob {

    private static final List<String> DEFAULT_CITY_QUERIES = Arrays.asList(
            "Mumbai", "Delhi", "Bangalore", "Chennai", "Hyderabad", "Kolkata", "Pune",
            "Ahmedabad", "Surat", "Jaipur", "Lucknow", "Kanpur", "Nagpur", "Visakhapatnam",
            "Bhopal", "Patna", "Vadodara", "Ludhiana", "Agra", "Nashik", "Faridabad",
            "Meerut", "Rajkot", "Varanasi", "Aurangabad", "Dhanbad", "Amritsar",
            "Ranchi", "Howrah", "Coimbatore", "Jabalpur", "Gwalior", "Vijayawada",
            "Jodhpur", "Madurai", "Raipur", "Kota", "Chandigarh", "Guwahati",
            "Solapur", "Hubli", "Mysore", "Tiruchirappalli", "Bareilly", "Aligarh",
            "Moradabad", "Thiruvananthapuram", "Noida", "Thane", "Indore", "Bhubaneswar",
            "Srinagar", "Mangalore", "Kochi", "Kozhikode", "Vijayawada", "Guntur"
    );

    private static final List<String> DEFAULT_REGION_QUERIES = Arrays.asList(
            "Maharashtra", "Delhi", "Karnataka", "Tamil Nadu", "Telangana",
            "West Bengal", "Gujarat", "Rajasthan", "Uttar Pradesh", "Bihar",
            "Madhya Pradesh", "Punjab", "Haryana", "Kerala", "Andhra Pradesh",
            "Jharkhand", "Assam", "Odisha", "Chhattisgarh", "Uttarakhand",
            "Himachal Pradesh", "Goa", "Jammu and Kashmir", "Tripura", "Meghalaya"
    );

    private static final List<String> DEFAULT_COUNTRY_QUERIES = Arrays.asList(
            "India",
            "United Arab Emirates",
            "United States",
            "United Kingdom",
            "Canada",
            "Australia",
            "Singapore"
    );

    @Autowired
    private MetaGeoLocationRepository metaGeoLocationRepository;

    @Autowired
    private FacebookAccountRepository facebookAccountRepository;

    private String clientId;
    private String locationTypeParam;
    private String countryCode;
    private String queriesParam;

    @Override
    public void run() {
        try {
            init();
            syncGeoLocations();
        } catch (Exception e) {
            log("Fatal error in MetaGeoLocationSyncTask: " + e.getMessage());
        }
    }

    private void syncGeoLocations() {

        if (clientId == null || clientId.isBlank()) {
            log("ERROR: client_id parameter is required");
            return;
        }

        FacebookAccount account =
                facebookAccountRepository.findByClientId(clientId).orElse(null);

        if (account == null || account.getAccessToken() == null) {
            log("ERROR: No Facebook account or access token found for client: "
                    + clientId);
            return;
        }

        String accessToken = account.getAccessToken();

        log("Starting geo-location sync for client=" + clientId
                + ", location_type=" + locationTypeParam
                + ", country_code=" + countryCode);

        boolean syncCities =
                "city".equalsIgnoreCase(locationTypeParam)
                || "both".equalsIgnoreCase(locationTypeParam);

        boolean syncRegions =
                "region".equalsIgnoreCase(locationTypeParam)
                || "both".equalsIgnoreCase(locationTypeParam);

        boolean syncCountries =
                "country".equalsIgnoreCase(locationTypeParam)
                || "both".equalsIgnoreCase(locationTypeParam);

        List<String> customQueries = queriesParam == null || queriesParam.isBlank()
                ? List.of()
                : Arrays.stream(queriesParam.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

        // ---------------- CITY SYNC ----------------

        if (syncCities) {

            List<String> cityQueries =
                    customQueries.isEmpty()
                            ? DEFAULT_CITY_QUERIES
                            : customQueries;

            log("Syncing cities — " + cityQueries.size() + " queries");

            syncLocations(
                    accessToken,
                    cityQueries,
                    MetaGeoLocation.TYPE_CITY,
                    countryCode
            );
        }

        // ---------------- REGION SYNC ----------------

        if (syncRegions) {

            List<String> regionQueries =
                    customQueries.isEmpty()
                            ? DEFAULT_REGION_QUERIES
                            : customQueries;

            log("Syncing regions — " + regionQueries.size() + " queries");

            syncLocations(
                    accessToken,
                    regionQueries,
                    MetaGeoLocation.TYPE_REGION,
                    countryCode
            );
        }

        // ---------------- COUNTRY SYNC ----------------

        if (syncCountries) {

            List<String> countryQueries =
                    customQueries.isEmpty()
                            ? DEFAULT_COUNTRY_QUERIES
                            : customQueries;

            log("Syncing countries — " + countryQueries.size() + " queries");

            syncLocations(
                    accessToken,
                    countryQueries,
                    MetaGeoLocation.TYPE_COUNTRY,
                    null
            );
        }

        log("Geo-location sync completed");
    }

    private void syncLocations(
            String accessToken,
            List<String> queries,
            String locationType,
            String countryCode) {

        int inserted = 0;
        int skipped = 0;

        for (String query : queries) {

            if (shouldStop()) {
                log("Stop requested — halting sync");
                break;
            }

            try {

                List<Map<String, String>> results =
                        MetaAdsApiService.searchGeoLocations(
                                accessToken,
                                query,
                                locationType
                        );

                for (Map<String, String> result : results) {

                    String metaKey = result.getOrDefault("key", "");
                    String name = result.getOrDefault("name", "");
                    String resCCode = result.getOrDefault("country_code", "");
                    String region = result.getOrDefault("region", "");

                    if (metaKey.isBlank() || name.isBlank()) {
                        continue;
                    }

                    // Apply country filtering only for city/region
                    if (!MetaGeoLocation.TYPE_COUNTRY.equals(locationType)) {

                        if (!resCCode.isBlank()
                                && !resCCode.equalsIgnoreCase(countryCode)) {
                            continue;
                        }
                    }

                    boolean exists =
                            metaGeoLocationRepository
                                    .existsByMetaKeyAndLocationType(
                                            metaKey,
                                            locationType
                                    );

                    if (exists) {
                        skipped++;
                        continue;
                    }

                    MetaGeoLocation entity = MetaGeoLocation.builder()
                            .metaKey(metaKey)
                            .name(name)
                            .locationType(locationType)
                            .countryCode(
                                    resCCode.isBlank()
                                            ? countryCode
                                            : resCCode
                            )
                            .regionName(region)
                            .syncedAt(new Date())
                            .build();

                    metaGeoLocationRepository.save(entity);

                    inserted++;
                }

                log("[" + locationType + "] '" + query
                        + "' → " + results.size() + " results");

                // Avoid Meta API rate limit
                Thread.sleep(200);

            } catch (InterruptedException ie) {

                Thread.currentThread().interrupt();

                log("Interrupted during geo-location sync");

                break;

            } catch (Exception e) {

                log("WARN: Failed to fetch '" + query
                        + "' (" + locationType + "): "
                        + e.getMessage());
            }
        }

        log(locationType
                + " sync done — inserted="
                + inserted
                + ", skipped(already exists)="
                + skipped);
    }

    private void init() {

        this.clientId =
                parameters.getString("client_id");

        this.locationTypeParam =
                parameters.getString("location_type", "both");

        this.countryCode =
                parameters.getString("country_code", "IN")
                        .trim()
                        .toUpperCase();

        this.queriesParam =
                parameters.getString("queries", "");
    }
}
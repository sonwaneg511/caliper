package com.caliper.keywordPlanner.service;

import java.io.IOException;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.keywordPlanner.dto.GoogleKeywordEstimate;
import com.caliper.keywordPlanner.dto.GoogleMonthlySearches;
import com.caliper.keywordPlanner.entity.GoogleExpansion;
import com.caliper.keywordPlanner.entity.SearchVolumePlan;
import com.caliper.keywordPlanner.repository.GoogleExpansionRepository;
import com.caliper.utils.campaign.GoogleSessionFactory;
import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v21.common.KeywordPlanHistoricalMetrics;
import com.google.ads.googleads.v21.common.MonthlySearchVolume;
import com.google.ads.googleads.v21.enums.KeywordPlanNetworkEnum.KeywordPlanNetwork;
import com.google.ads.googleads.v21.enums.MonthOfYearEnum.MonthOfYear;
import com.google.ads.googleads.v21.services.GenerateKeywordHistoricalMetricsRequest;
import com.google.ads.googleads.v21.services.GenerateKeywordHistoricalMetricsResponse;
import com.google.ads.googleads.v21.services.GenerateKeywordHistoricalMetricsResult;
import com.google.ads.googleads.v21.services.GenerateKeywordIdeaResult;
import com.google.ads.googleads.v21.services.GenerateKeywordIdeasRequest;
import com.google.ads.googleads.v21.services.GeoTargetConstantServiceClient;
import com.google.ads.googleads.v21.services.KeywordPlanIdeaServiceClient;
import com.google.ads.googleads.v21.services.KeywordPlanIdeaServiceClient.GenerateKeywordIdeasPagedResponse;
import com.google.ads.googleads.v21.services.KeywordSeed;
import com.google.ads.googleads.v21.services.SuggestGeoTargetConstantsRequest;
import com.google.ads.googleads.v21.services.SuggestGeoTargetConstantsRequest.LocationNames;
import com.google.ads.googleads.v21.services.SuggestGeoTargetConstantsResponse;
import com.google.ads.googleads.v21.services.UrlSeed;

@Service
public class KeywordService {

	private static final String DEFAULT_LANGUAGE_CONSTANT = "languageConstants/1000";

	@Autowired
	private GoogleExpansionRepository googleExpansionRepository;

	public void expandGoogleKeyword(Long planId, GoogleAdsClient googleAdsClient, String seedKeyword, int page,
			int pageSize, String customerId, String keywordExpansionType, String source, String keywordPlanNetwork) {

		GenerateKeywordIdeasRequest.Builder requestBuilder = GenerateKeywordIdeasRequest.newBuilder()
				.setCustomerId(customerId)
				.setLanguage(DEFAULT_LANGUAGE_CONSTANT)
				.setKeywordPlanNetwork(KeywordPlanNetwork.valueOf(keywordPlanNetwork));

		if (SearchVolumePlan.KEYWORD_EXPANSION_TYPE_SEED_URL.equalsIgnoreCase(keywordExpansionType)) {
			requestBuilder.setUrlSeed(UrlSeed.newBuilder().setUrl(seedKeyword).build());
		} else {
			requestBuilder.setKeywordSeed(KeywordSeed.newBuilder().addKeywords(seedKeyword).build());
		}

		try (KeywordPlanIdeaServiceClient keywordPlanIdeaServiceClient =
				googleAdsClient.getLatestVersion().createKeywordPlanIdeaServiceClient()) {

			GenerateKeywordIdeasPagedResponse response =
					keywordPlanIdeaServiceClient.generateKeywordIdeas(requestBuilder.build());

			for (GenerateKeywordIdeaResult result : response.iterateAll()) {
				KeywordPlanHistoricalMetrics metrics = result.getKeywordIdeaMetrics();

				GoogleExpansion expansion = new GoogleExpansion();
				expansion.setPlanID(planId);
				expansion.setSeedKeyword(seedKeyword);
				expansion.setExpandedKeyword(result.getText());
				expansion.setSearchVolume(metrics.getAvgMonthlySearches());
				expansion.setInsertedDate(new Date());
				expansion.setCaliperInsertion(false);

				googleExpansionRepository.save(expansion);
			}
		}
	}

	public List<GoogleKeywordEstimate> getKeywordsReachEstimate(Set<String> keywords, String city,
			KeywordPlanNetwork network, long batchSize, String customerId) throws IOException {

		GoogleAdsClient googleAdsClient = GoogleSessionFactory.getGoogleAdsClient();
		String geoTargetConstant = resolveGeoTargetConstant(googleAdsClient, city);

		GenerateKeywordHistoricalMetricsRequest request = GenerateKeywordHistoricalMetricsRequest.newBuilder()
				.setCustomerId(customerId)
				.setLanguage(DEFAULT_LANGUAGE_CONSTANT)
				.addGeoTargetConstants(geoTargetConstant)
				.setKeywordPlanNetwork(network)
				.addAllKeywords(keywords)
				.build();

		List<GoogleKeywordEstimate> estimates = new ArrayList<>();

		try (KeywordPlanIdeaServiceClient keywordPlanIdeaServiceClient =
				googleAdsClient.getLatestVersion().createKeywordPlanIdeaServiceClient()) {

			GenerateKeywordHistoricalMetricsResponse response =
					keywordPlanIdeaServiceClient.generateKeywordHistoricalMetrics(request);

			for (GenerateKeywordHistoricalMetricsResult result : response.getResultsList()) {
				KeywordPlanHistoricalMetrics metrics = result.getKeywordMetrics();

				List<GoogleMonthlySearches> monthlySearches = new ArrayList<>();
				for (MonthlySearchVolume volume : metrics.getMonthlySearchVolumesList()) {
					monthlySearches.add(toMonthlySearches(volume));
				}
				monthlySearches.sort(Comparator.comparing((GoogleMonthlySearches m) -> m.getYear())
						.thenComparing(GoogleMonthlySearches::getMonth));

				estimates.add(new GoogleKeywordEstimate(result.getText(), metrics.getAvgMonthlySearches(), city,
						monthlySearches));
			}
		}

		return estimates;
	}

	public List<GoogleExpansion> getAllGoogleExpansionByPlanID(Long planId) {
		return googleExpansionRepository.findAllByPlanID(planId);
	}

	private GoogleMonthlySearches toMonthlySearches(MonthlySearchVolume volume) {
		MonthOfYear monthOfYear = volume.getMonth();
		String month = String.format("%02d", Month.valueOf(monthOfYear.name()).getValue());
		String year = String.valueOf(volume.getYear());
		return new GoogleMonthlySearches(month, year, volume.getMonthlySearches());
	}

	private String resolveGeoTargetConstant(GoogleAdsClient googleAdsClient, String city) {
		try (GeoTargetConstantServiceClient geoTargetConstantServiceClient =
				googleAdsClient.getLatestVersion().createGeoTargetConstantServiceClient()) {

			SuggestGeoTargetConstantsRequest request = SuggestGeoTargetConstantsRequest.newBuilder()
					.setLocale("en")
					.setLocationNames(LocationNames.newBuilder().addNames(city).build())
					.build();

			SuggestGeoTargetConstantsResponse response =
					geoTargetConstantServiceClient.suggestGeoTargetConstants(request);

			if (response.getGeoTargetConstantSuggestionsCount() == 0) {
				throw new IllegalStateException("No geo target constant found for city: " + city);
			}

			return response.getGeoTargetConstantSuggestions(0).getGeoTargetConstant().getResourceName();
		}
	}
}

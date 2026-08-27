package com.caliper.location.dto.response;
import java.util.Map;

import com.caliper.location.entity.DealerLocation;
import com.caliper.location.facebook.entity.FacebookCategory;
import com.caliper.location.gmb.entity.GMBCategory;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewLocationDetails {
	
	@JsonProperty("client_name")
	private String clientName;
	
	@JsonProperty("website_url")
	private String websiteUrl;
	
	@JsonProperty("map_url")
	private String mapUrl;
	
	@JsonProperty("review_url")
	private String reviewUrl;
	
	@JsonProperty("dealer_location")
	private DealerLocation dealerLocation;
	
	@JsonProperty("gmb_category_id_and_name")
	private Map<Long, GMBCategory> gmbCategoryIdVsName;

	@JsonProperty("fb_category_id_and_name")
	private Map<Long, FacebookCategory> fbCategoryIdVsName;
	
}
package com.caliper.campaign.google.dto.response;

import java.util.Date;
import java.util.List;

import com.caliper.review.dto.response.ReviewResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewPageableCampaignResponse {

	@JsonProperty("view_all_campaigns_response")
	List<ViewAllCampaignsResponse> viewAllCampaignsResponse;
	
    @JsonProperty("total_no_of_pages")
    public int totalNoOfPages;
    
    @JsonProperty("total_no_of_records")
    public Long totalNoOfRecords;
}

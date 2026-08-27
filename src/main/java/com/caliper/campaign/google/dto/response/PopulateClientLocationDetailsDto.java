package com.caliper.campaign.google.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PopulateClientLocationDetailsDto {
	
	@JsonProperty("client_id")
	public String clientId;
	
	@JsonProperty("client_name")
	public String clientName;
	
	@JsonProperty("platform")
	public String platform;
	
	@JsonProperty("youtube_video_url")
	public String youtubeVideoUrl;
	
	@JsonProperty("campaign_location_details")
	private List<CampaignLocationDetails> campaignLocationDetails;

}

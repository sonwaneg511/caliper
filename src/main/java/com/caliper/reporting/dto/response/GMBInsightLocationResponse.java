package com.caliper.reporting.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMBInsightLocationResponse {
	
	public long id;
	@JsonProperty("location_name")
    public String locationName;
	@JsonProperty("search_views")
    public double searchViews;
	@JsonProperty("map_views")
    public double mapViews;
	@JsonProperty("total_views")
    public double totalViews;
	@JsonProperty("driving_direction_actions")
    public double drivingDirectionActions;
	@JsonProperty("website_actions")
    public double websiteActions;
	@JsonProperty("total_actions")
    public double totalActions;

}

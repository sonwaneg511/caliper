package com.caliper.reporting.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GMBInsightLocationTableData {

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

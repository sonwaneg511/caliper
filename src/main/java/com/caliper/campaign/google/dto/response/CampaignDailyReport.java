package com.caliper.campaign.google.dto.response;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignDailyReport {
	
	@JsonProperty("table_data")
	private List<CampaignReportDaily> campaignReportDaily; //Clicks * CPC
	@JsonProperty("total_no_of_pages")
    public int totalNoOfPages;
    @JsonProperty("total_no_of_records")
    public int totalNoOfRecords;
	
	
	

}

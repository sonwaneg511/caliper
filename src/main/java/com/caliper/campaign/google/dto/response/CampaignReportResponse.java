package com.caliper.campaign.google.dto.response;

import java.util.Date;
import java.util.List;

import com.caliper.post.dto.Response.CaliperResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CampaignReportResponse extends CaliperResponse{

	@JsonProperty("start_date")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
	private Date startDate;
	
	@JsonProperty("end_date")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
	private Date endDate;
	@JsonProperty("consolidate_campaign_report")
	private List<ConsolidateCampaignReport> consolidateCampaignReport;
	@JsonProperty("report")
	private Report report;
	

}
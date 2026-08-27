package com.caliper.campaign.google.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class Report {

	@JsonProperty("campaign_wise_report")
	private CampaignReport campaignReport;
//	@JsonProperty("export_daily_data")
//	private List<ExportCampaignDailyReport> exportDailyData;
	@JsonProperty("campaign_daily_reports")
	private CampaignDailyReport campaignDailyReports;
}

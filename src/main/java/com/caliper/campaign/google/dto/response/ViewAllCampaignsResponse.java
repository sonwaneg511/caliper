package com.caliper.campaign.google.dto.response;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewAllCampaignsResponse {
	
	@JsonProperty("client_id")
	public String clientId;
	
	@JsonProperty("campaign_id")
	public Long campaignId;
	
	@JsonProperty("campaign_name")
	public String campaignName;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	@JsonProperty("start_date")
	public Date startDate;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	@JsonProperty("end_date")
	public Date endDate;
	
	@JsonProperty("total_budget")
	public BigDecimal totalBudget;
	
	@JsonProperty("platform")
	public String platform;
	
	@JsonProperty("status")
	public String status;
	
	@JsonProperty("location_detail")
	public String locationDetail;
}
package com.caliper.campaign.google.dto.request;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignReportActionRequest {

	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("google_account_id")
	private long googleAccountId;
	
	@JsonProperty("start_date")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
	private Date startDate;
	
	@JsonProperty("end_date")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
	private Date endDate;
	@JsonProperty("page_no")
	public int pageNo;
}

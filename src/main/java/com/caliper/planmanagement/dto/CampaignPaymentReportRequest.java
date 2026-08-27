package com.caliper.planmanagement.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CampaignPaymentReportRequest {

	@JsonProperty("start_date")
	private Date startDate;

	@JsonProperty("end_date")
	private Date endDate;

	@JsonProperty("client_id")
	private String clientId;

	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("page_no")
	private int pageNo;

	@JsonProperty("page_size")
	private int pageSize;
}

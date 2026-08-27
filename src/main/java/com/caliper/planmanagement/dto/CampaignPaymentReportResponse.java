package com.caliper.planmanagement.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CampaignPaymentReportResponse {

	@JsonProperty("status")
	private int status;

	@JsonProperty("message")
	private String message;

	@JsonProperty("payments")
	private List<CampaignPaymentDetailDto> payments;

	@JsonProperty("total_no_of_pages")
	private int totalNoOfPages;

	@JsonProperty("total_no_of_records")
	private long totalNoOfRecords;
}

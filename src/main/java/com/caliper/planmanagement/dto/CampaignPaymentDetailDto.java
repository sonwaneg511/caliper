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
public class CampaignPaymentDetailDto {

	@JsonProperty("campaign_name")
	private String campaignName;

	@JsonProperty("location")
	private String location;

	@JsonProperty("start_date")
	private Date startDate;

	@JsonProperty("end_date")
	private Date endDate;

	@JsonProperty("campaign_status")
	private String campaignStatus;

	@JsonProperty("schedule_status")
	private String scheduleStatus;

	@JsonProperty("duration")
	private long duration;

	@JsonProperty("amount_paid")
	private double amountPaid;

	@JsonProperty("amount_per_day")
	private double amountPerDay;

	@JsonProperty("payment_date")
	private Date paymentDate;
}

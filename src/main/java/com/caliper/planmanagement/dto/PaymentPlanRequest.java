package com.caliper.planmanagement.dto;

import java.util.Date;
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
public class PaymentPlanRequest {
	
	@JsonProperty("payment_id")
	private String paymentId;
	
	@JsonProperty("order_id")
	private String orderId;
	
	@JsonProperty("amount")
	private double amount;
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("plan_id")
	private long planId;
	
	@JsonProperty("campaign_id")
	private long campaignId;
	
	@JsonProperty("payment_method")
	private String paymentMethod;
	
	@JsonProperty("transaction_date_time")
	private Date transactionDatetime;
	
	@JsonProperty("error_description")
	private String errorDescription;
	
	@JsonProperty("amount_refunded")
	private String amountRefunded;
	
	@JsonProperty("inserted_date")
	private Date insertedDate;
	
	@JsonProperty("is_plan")
	private boolean isPlan;
	
	@JsonProperty("is_campaign")
	private boolean isCampaign;
	
}

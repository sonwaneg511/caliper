package com.caliper.dashboard.dto.campaign;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("summary")
public class CampaignSummary {

	private BigDecimal totalSpends;
	private Double impressions;
	private Long totalCampaigns;
	private Long activeCampaigns;
}

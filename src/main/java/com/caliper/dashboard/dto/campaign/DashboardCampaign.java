package com.caliper.dashboard.dto.campaign;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("campaign")
public class DashboardCampaign {
	
	private CampaignSummary summary;
    private List<MonthlySpend> spends; 

}

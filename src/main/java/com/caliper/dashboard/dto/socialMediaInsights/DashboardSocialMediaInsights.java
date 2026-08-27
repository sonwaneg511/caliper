package com.caliper.dashboard.dto.socialMediaInsights;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSocialMediaInsights {

	private Long followers;
	private Long totalLikes;
	private Long totalShares;
}

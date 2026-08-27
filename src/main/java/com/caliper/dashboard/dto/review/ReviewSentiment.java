package com.caliper.dashboard.dto.review;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSentiment {

	private List<PieData> pieData;

}

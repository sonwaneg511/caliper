package com.caliper.dashboard.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostsGraphData {

	private String month;
	private Long totalGMBPosts;
	private Long totalFbPosts;
}

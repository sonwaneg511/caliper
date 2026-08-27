package com.caliper.dashboard.dto.post;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DashboardPosts {

	private PostsSummary postsSummary;
	private List<PostLocation>topPostLocations;
	private List<PostLocation>leastPostLocations;
	private List<PostsGraphData>postsGraphData;
}

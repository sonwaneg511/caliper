package com.caliper.reporting.dto.response;

import java.util.List;

import com.caliper.dashboard.dto.post.PostsGraphData;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostReportingResponse {

	@JsonProperty("total_posts")
	public long totalPosts;
	@JsonProperty("pending_deployment")
	public long pendingPosts;
	@JsonProperty("no_of_post_graph")
	public List<PostsGraphReportingData> postGraphReportingData;
	@JsonProperty("post_table_data")
	public ReportingPostTableData postTableData;


}

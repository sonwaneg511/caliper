package com.caliper.reporting.dto.response;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class ReviewReportingDataPageResponse {
	@JsonProperty("review_overview_data")
	public ReviewOverViewData reviewOverViewData;
	
	@JsonProperty("sentiment_review")
	public List<SentimentReview> sentimentReview;
	
	@JsonProperty("comment_split_review")
	public List<CommentSplitReview> commentSplitReview;
	
	@JsonProperty("review_graph_data")
	public List<ReviewGraphData> reviewGraphData;
	
	@JsonProperty("rating_count_data")
	public List<RatingCountData> ratingCountData;

	@JsonProperty("review_table_data")
	public ReviewTableData reviewTableData;


}

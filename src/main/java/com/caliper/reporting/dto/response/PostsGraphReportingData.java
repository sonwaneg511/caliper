package com.caliper.reporting.dto.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Builder;

@Builder
public class PostsGraphReportingData {

	@JsonProperty("date")
	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date date;
	@JsonProperty("google")
	private Long totalGMBPosts;
	@JsonProperty("facebook")
	private Long totalFbPosts;
}

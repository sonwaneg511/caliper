package com.caliper.utils.gemini.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAdCopyResponse {

	@JsonProperty("client_id")
	private String clientId;

	@JsonProperty("headlines")
	private List<String> headlines;

	@JsonProperty("long_headlines")
	private List<String> longHeadlines;

	@JsonProperty("descriptions")
	private List<String> descriptions;

}

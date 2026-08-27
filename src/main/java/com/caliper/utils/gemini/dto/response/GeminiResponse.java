package com.caliper.utils.gemini.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GeminiResponse {
	
	@Expose
	@JsonProperty("candidates")
	private List<Candidate> candidates;
	
	@Expose
	@JsonProperty("usage_medadata")
	private UsageMetadata usageMetadata;
	
	@Expose
	@JsonProperty("model_version")
	private String modelVersion;
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	public class Candidate{
		@Expose
		@JsonProperty("content")
		private Content content;
		
		@Expose
		@JsonProperty("finish_reason")
		private String finishReason;
		
		@Expose
		@JsonProperty("avg_logprobs")
		private double avgLogprobs;
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	public class Part{
		@Expose
		private String text;
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	public class Content{
		@Expose
		private List<Part> parts;
		
		@Expose
		private String role;
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	class UsageMetadata {
		@Expose
		@JsonProperty("prompt_token_count")
		private int promptTokenCount;
		
		@Expose
		@JsonProperty("candidates_token_count")
		private int candidatesTokenCount;
		
		@Expose
		@JsonProperty("total_token_count")
		private int totalTokenCount;
		
		@Expose
		@JsonProperty("promp_tokens_details")
		private List<TokenDetail> promptTokensDetails;
		
		@Expose
		@JsonProperty("candidates_tokens_details")
		private List<TokenDetail> candidatesTokensDetails;
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	class TokenDetail {
		@Expose
		private String modality;
		
		@Expose
		@JsonProperty("token_count")
		private int tokenCount;
	}
}

package com.caliper.post.dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostLocationDetailsRequest {

	@JsonProperty("client_id")
	public String clientId;
	@JsonProperty("user_id")
	public String userId;
	@JsonProperty("post_id")
	public long postId;
	public String platform;
	
}

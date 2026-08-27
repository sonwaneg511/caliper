package com.caliper.post.dto.Response;

import com.google.gson.annotations.SerializedName;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostGraphResponse {

	@SerializedName("total_posts")
	public long totalPosts;
	@SerializedName("pending_posts")
	public long pendingPosts;
	@SerializedName("deployed_posts")
	public long deployedPosts;
	@SerializedName("total_likes")
	public long totalLikes;
	@SerializedName("total_comments")
	public long totalComments;
	@SerializedName("total_share")
	public long totalShares;
	
}

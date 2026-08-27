package com.caliper.post.dto.Response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDataResponse {

	
	@JsonProperty("post_id")
	public long postId;
    public String status;
    public String image;
    public String title;
    public String label;
    public String description;
    public long likes;
    public long comments;
    public long shares;
    public long dealers;
    public Date date;
    
}

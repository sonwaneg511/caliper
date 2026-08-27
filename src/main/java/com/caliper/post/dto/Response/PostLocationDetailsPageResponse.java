package com.caliper.post.dto.Response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PostLocationDetailsPageResponse {
	
	@JsonProperty("post_location_details")
	List<PostViewDetailsResponse> postViewDetailsResponseList;
	
    @JsonProperty("total_no_of_pages")
    public int totalNoOfPages;
    
    @JsonProperty("total_no_of_records")
    public Long totalNoOfRecords;

}

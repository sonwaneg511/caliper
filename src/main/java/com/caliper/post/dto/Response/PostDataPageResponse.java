package com.caliper.post.dto.Response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostDataPageResponse {

	@JsonProperty("post_data")
	List<PostDataResponse> postDataResponseList;
	
    @JsonProperty("total_no_of_pages")
    public int totalNoOfPages;
    
    @JsonProperty("total_no_of_records")
    public Long totalNoOfRecords;




}

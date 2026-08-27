package com.caliper.review.dto.response;


import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPageResponse {

	@JsonProperty("review_response")
	List<ReviewResponse> reviewResponseList;
	
    @JsonProperty("total_no_of_pages")
    public int totalNoOfPages;
    
    @JsonProperty("total_no_of_records")
    public Long totalNoOfRecords;


}

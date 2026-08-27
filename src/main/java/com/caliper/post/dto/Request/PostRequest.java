package com.caliper.post.dto.Request;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PostRequest {

	@JsonProperty("client_id")
	public String clientId;
	
	@JsonProperty("user_id")
	public String userId;
	
	@JsonProperty("start_date")
	public Date startDate;
	
	@JsonProperty("end_date")
	public Date endDate;
	
	public String state;
	public String city;
	
	@JsonProperty("dealer_id")
	public List<String> dealerId;
	
	public String platform;
	
	@JsonProperty("status")
	public String status;
	
	@JsonProperty("page_no")
	public int pageNo;

	@JsonProperty("post_id")
	public long postId;

}

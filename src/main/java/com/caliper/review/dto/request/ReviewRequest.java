package com.caliper.review.dto.request;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {

	@JsonProperty("client_id")
	public String clientId;       //location level
	
	@JsonProperty("user_id")
	public String userId;            
	
	public String state;         //location level
	
	public String city;          //location level
	
	@JsonProperty("dealer_id")
	public List<String> dealerId;        //location level
	
	@JsonProperty("rating_range")
	public int ratingRange;         //review level 1,2,3,4,5
	
	public String replied;           //replied or not
	
	@JsonProperty("rating_type")
	public String ratingType;         //Rating with review and Rating without review
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("start_date")
	public Date startDate;                      //created time
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("end_date")
	public Date endDate;                       //created time
	
	public String platform;
	
	@JsonProperty("page_no")
	public int pageNo;
	
	public static final String REPLY_STATUS_DRAFTED = "reply_drafted";
	public static final String REPLY_STATUS_NO = "no_reply";
	public static final String REPLY_STATUS_DEPLOYED = "reply_deployed";
	
	public static final String REVIEW_WITH_COMMENT = "review_with_comment";
	public static final String REVIEW_WITHOUT_COMMENT = "review_without_comment";
	
	public static final String REVIEW_STATUS = "reviewStatus";
	public static final String REPLY_STATUS = "replyStatus";
	public static final String STAR_RATING = "starRating";
	public static final String CREATED_TIME = "createdTime";
	public static final String DEALER_ID = "dealerId";
	public static final String CLIENT_ID = "clientId";
	
	public static final String SUCCESS = "sucess";
	public static final String FAILURE = "failure";
}

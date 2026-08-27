package com.caliper.review.dto.request;


import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewGraphDTO {

	private long starRating;
    private Date createdTime;
    private String replyStatus;
 
}

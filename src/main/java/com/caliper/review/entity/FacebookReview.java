package com.caliper.review.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facebook_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookReview{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "dealer_id")
	private String dealerId;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@Column(name = "reply_time")
	private Date replyTime;
	
	@Column(name = "star_rating")
	private long starRating;
	
	@Column(name = "review_id")
	private String reviewId;
	
	@Column(name = "comment", columnDefinition = "Text")
	private String comment;
	
	@Column(name = "reply_comment", columnDefinition = "Text")
	private String replyComment;
	
	@Column(name = "reviewer")
	private String reviewer;
	
	@Column(name = "reply_status")
	private String replyStatus;
	
	@Column(name = "review_status")
	private boolean reviewStatus;
	
	@Column(name = "replied_by")
	private String repliedBy;	
	
	@Column(name = "inserted_date")
	private Date insertedDate;
	
	@Column(name = "facebook_page_id")
	private String facebookPageId;	
	
	public static final String REVIEW_RATING_MAX = "5";
	public static final String REVIEW_RATING_MIN = "1";
	public static final int PAGE_SIZE = 10;
	
	public static final String REPLY_STATUS_DRAFTED = "reply_drafted";
	public static final String REPLY_STATUS_NO = "no_reply";
	public static final String REPLY_STATUS_DEPLOYED = "reply_deployed";
	
	public static final String REPLIED_BY_AUTO = "auto";
	public static final String REPLIED_BY_UNKNOWN = "unknown";
	public static final String STATUS_NEW = "New";
	
	public FacebookReview(String reviewId, String facebookPageId, String dealerId, long starRating, String comment, String replyComment, 
			String reviewer,  String replyStatus, boolean reviewStatus, Date createdTime, Date replyTime, 
			String repliedBy, Date insertedDate) {
		super();
		this.dealerId = dealerId;
		this.createdTime = createdTime;
		this.replyTime = replyTime;
		this.starRating = starRating;
		this.reviewId = reviewId;
		this.comment = comment;
		this.replyComment = replyComment;
		this.reviewer = reviewer;
		this.replyStatus = replyStatus;
		this.reviewStatus = reviewStatus;
		this.repliedBy = repliedBy;
		this.insertedDate = insertedDate;
		this.facebookPageId = facebookPageId;
	}
	
	
	
}

package com.caliper.review.entity;

import java.time.Instant;
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
@Table(name = "gmb_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMBReview{
	    
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "dealer_id")
	private String dealerId;
	
	@Column(name = "created_time")
	private Date createdTime;         //2024-01-17 11:27:17
	
	@Column(name = "reply_time")
	private Date replyTime;           //2024-01-17 11:37:26
	
	@Column(name = "star_rating")
	private long starRating;         //1,2,3,4,5
	 
	@Column(name = "review_id")
	private String reviewId;      //e.g. AbFvOqk-2f3TD15YB2o7rvXi6rv_g2k4cBeGgZXdIVn9jdFfjDRw0D_BN81EjoFEnyxd6QgnQvPELQ

    @Column(name = "review_name")
	private String reviewName;    //e.g. accounts/109981476430152435974/locations/4068947158648172940/reviews/AbFvOqk-2f3TD15YB2o7rvXi6rv_g2k4cBeGgZXdIVn9jdFfjDRw0D_BN81EjoFEnyxd6QgnQvPELQ

	@Column(name = "comment", columnDefinition = "Text")
	private String comment;      //e.g.Awesome collection of philps light with complete ranges...

	@Column(name = "reply_comment", columnDefinition = "Text")
	private String replyComment;   //e.g.Dear Ashik Husain Husain,Thank you for your feedback! We are glad you had a good experience.

	@Column(name = "reviewer")
	private String reviewer;       //e.g.Ashik Husain Husain

	@Column(name = "reply_status") 
	private String replyStatus;     //reply_deployed,no_reply,reply_drafted

	@Column(name = "review_status")
	private Boolean reviewStatus;   //replied or not

	@Column(name = "replied_by")
	private String repliedBy;       //user id

	@Column(name = "inserted_date")
	private Date insertedDate;           //2024-07-08 00:28:34

	@Column(name = "instant_date")
	private Instant instantDate;

    @Column(name = "edited_date")
	private Date editedDate;
	
    public static final String STAR_RATING_ONE = "ONE";
	public static final String STAR_RATING_TWO = "TWO";
	public static final String STAR_RATING_THREE = "THREE";
	public static final String STAR_RATING_FOUR = "FOUR";
	public static final String STAR_RATING_FIVE = "FIVE";
	public static final String STAR_RATING_NOT_SPECIDIED = "STAR_RATING_UNSPECIFIED";
	
	public static final String RATING_WITH_REVIEW = "Rating With Review";
	public static final String RATING_WITHOUT_REVIEW = "Rating Without Review";
	
	public static final String REPLY_STATUS_DRAFTED = "reply_drafted";
	public static final String REPLY_STATUS_NO = "no_reply";
	public static final String REPLY_STATUS_DEPLOYED = "reply_deployed";
	public static final String STATUS_NEW = "New";

	public static final String REPLIED_BY_AUTO = "auto";
	public static final String REPLIED_BY_UNKNOWN = "unknown";
	

	
	public GMBReview(String reviewId, String dealerId, String reviewName, long starRating,
			String comment, String replyComment, String reviewer, String replyStatus, boolean reviewStatus,
			Date createdTime, Date replyTime, String repliedBy, Date insertedDate, Instant instantDate) {
		
		this.dealerId = dealerId;
		this.createdTime = createdTime;
		this.replyTime = replyTime;
		this.starRating = starRating;
		this.reviewId = reviewId;
		this.reviewName = reviewName;
		this.comment = comment;
		this.replyComment = replyComment;
		this.reviewer = reviewer;
		this.replyStatus = replyStatus;
		this.reviewStatus = reviewStatus;
		this.repliedBy = repliedBy;
		this.insertedDate = insertedDate;
		this.instantDate = instantDate;
	}
}

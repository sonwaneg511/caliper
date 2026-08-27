package com.caliper.test.entity;


import java.util.Date;

import lombok.Data;

@Data
public class GMBReviewTest {

	public static final String SQL_TABLE = "gmb_review";
    public static final String SQL_COLUMN_REVIEW_ID = "review_id";
    public static final String SQL_COLUMN_LOCATION_ID = "location_id";
    public static final String SQL_COLUMN_DEALER_ID = "dealer_id";
    public static final String SQL_COLUMN_REVIEW_NAME = "review_name";
    public static final String SQL_COLUMN_STAR_RATING = "star_rating";
    public static final String SQL_COLUMN_COMMENT = "comment";
    public static final String SQL_COLUMN_REPLY_COMMENT = "reply_comment";
    public static final String SQL_COLUMN_REVIEWER = "reviewer";
    public static final String SQL_COLUMN_REPLY_STATUS = "reply_status";
    public static final String SQL_COLUMN_REVIEW_STATUS = "review_status";
    public static final String SQL_COLUMN_CREATED_TIME = "created_time";

    private String reviewId;
    private long locationId;
    private String dealerId;
    private String reviewName;
    private long starRating;
    private String comment;
    private String replyComment;
    private String reviewer;
    private String replyStatus;
    private boolean reviewStatus;
    private Date createdTime;
}

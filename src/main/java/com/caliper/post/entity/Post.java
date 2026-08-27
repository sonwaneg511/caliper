package com.caliper.post.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post", indexes = { @Index(name = "idx_platform_created", columnList = "platform, created_date"),
		@Index(name = "idx_created_date", columnList = "created_date"),
		@Index(name = "idx_post_type", columnList = "post_type"),
		@Index(name = "idx_start_end_date", columnList = "start_date, end_date"),
		@Index(name = "idx_status", columnList = "status") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Long postId;

	@Column(name = "client_id", length = 1000)
	private String clientId;

	@Enumerated(EnumType.STRING)
	@Column(name = "platform", nullable = false, length = 20)
	private Platform platform;

	@Column(name = "post_type", nullable = false, length = 50)
	private String postType;

	@Column(name = "offer_title", length = 400)
	private String offerTitle;

	@Column(name = "summary", columnDefinition = "TEXT")
	private String summary;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_date")
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "image_url", length = 2000)
	private String imageUrl;

	@Column(name = "media_format", length = 50)
	private String mediaFormat;

	@Column(name = "action_type", length = 50)
	private String actionType;

	@Column(name = "action_url", length = 2000)
	private String actionUrl;

	@Column(name = "coupon_code", length = 100)
	private String couponCode;

	@Column(name = "redeem_url", length = 2000)
	private String redeemUrl;

	@Column(name = "terms_conditions", columnDefinition = "TEXT")
	private String termsConditions;

	@Column(name = "created_by", length = 100)
	private String createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date", nullable = false)
	private Date createdDate;

	@Column(name = "comment", columnDefinition = "TEXT")
	private String comment;

	@Column(name = "status", nullable = false, length = 30)
	private String status;

	public static final String STATUS_DEPLOYED = "deployed";
	public static final String STATUS_SUBMIT = "submit";
	public static final String STATUS_PROCESSING = "processing";
	public static final String STATUS_ERROR = "error";

	public static final String MEDIA_TYPE_PHOTO = "PHOTO";
	public static final String MEDIA_TYPE_VIDEO = "VIDEO";

	public static final String POST_TYPE_EVENT = "event";
	public static final String POST_TYPE_OFFER = "offer";
	public static final String POST_TYPE_STORE_PAGE_OFFER = "store page offer";
	public static final String POST_TYPE_WHATS_NEW = "whats_new";
	public static final String POST_TYPE_COVID19 = "covid19";
	public static final String POST_TYPE_LINK = "Link";
	public static final String POST_TYPE_PHOTO = "Photo";
	public static final String POST_TYPE_TEXT = "Text";
	public static final String POST_TYPE_CAROUSEL = "Carousel";

	public static final String ACTION_TYPE_BOOK = "BOOK";
	public static final String ACTION_TYPE_ORDER = "ORDER";
	public static final String ACTION_TYPE_SHOP = "SHOP";
	public static final String ACTION_TYPE_LEARN_MORE = "LEARN_MORE";
	public static final String ACTION_TYPE_SIGN_UP = "SIGN_UP";
	public static final String ACTION_TYPE_CALL = "CALL";
	public static final String ACTION_TYPE_UNSPECIFIED = "ACTION_TYPE_UNSPECIFIED";

	public static final String CITY_ALL = "all";
	public static final String TEMPLATE_TOKEN_CITY = "city";
	public static final String TEMPLATE_TOKEN_STATE = "state";
	public static final String TEMPLATE_TOKEN_AREA = "area";
	public static final String TEMPLATE_TOKEN_DEALER_ID = "dealer-id";
	public static final String TEMPLATE_TOKEN_MAP_URL = "map-url";
	public static final String TEMPLATE_TOKEN_WEBSITE_URL = "website-url";
	public static final String TEMPLATE_TOKEN_DEALER_NAME = "dealer-name";
	public static final String TEMPLATE_TOKEN_PHONE_NUMBER = "phone-number";
	public static final String TEMPLATE_TOKEN_TYPE = "type";
	public static final String TEMPLATE_TOKEN_ADDRESS = "address";

}

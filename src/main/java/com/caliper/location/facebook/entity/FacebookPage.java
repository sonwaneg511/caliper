package com.caliper.location.facebook.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "facebook_page")
public class FacebookPage {

	@Id
	@Column(name = "facebook_page_id")
	private Long facebookPageId;

	@Column(name = "client_id", nullable = false)
	private String clientId;

	// null = shared across all dealers for this client; non-null = dealer-specific
	@Column(name = "dealer_id")
	private String dealerId;

	@Column(name = "app_client_id")
	private Long appClientId;

	@Column(name = "instagram_page_id")
	private Long instagramPageId;

	@Column(name = "instagram_account_id")
	private Long instagramAccountId;

	@Column(name = "app_client_secret")
	private String appClientSecret;

	@Column(name = "user_access_token", length = 2000)
	private String userAccessToken;

	@Column(name = "page_access_token", length = 2000)
	private String pageAccessToken;

	@Column(name = "last_modified_by")
	private String lastModifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_modified_date")
	private Date lastModifiedDate;

	@Column(name = "page_name")
	private String pageName;

}

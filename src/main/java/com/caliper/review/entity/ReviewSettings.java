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
@Table(name = "review_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "platform")
	private String platform;
	
	@Column(name = "reviews_last_inserted_date")
	private String reviewLastInsertedDate;
	
	@Column(name = "auto_response_date")
	private Date autoResponseDate;
	
	public static final String PLATFORM_GMB = "GMB";
	public static final String PLATFORM_FACEBOOK = "FACEBOOK";
}


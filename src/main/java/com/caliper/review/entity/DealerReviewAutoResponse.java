package com.caliper.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dealer_review_auto_response")
public class DealerReviewAutoResponse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String clientId;
	
	private String source;

	private Long rating;

	@Column(name = "review_comment")
	private String reviewComment;

	@Column(name = "with_comment")
	private Boolean withComment;

	@Column(name = "dealer_type")
	private String dealerType;
	
}

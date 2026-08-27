package com.caliper.campaign.google.entity;

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
@Table(name = "google_keyword")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleKeyword {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "adgroup_id")
	private long adgroupId;
	
	@Column(name = "keyword")
	private String keyword;
	
	@Column(name = "keyword_resource_name")
	private String keywordResourceName;
	
	@Column(name = "match_type")
	private String matchType;
}

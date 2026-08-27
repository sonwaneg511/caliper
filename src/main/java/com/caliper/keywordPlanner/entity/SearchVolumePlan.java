package com.caliper.keywordPlanner.entity;

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
@Table(name = "search_volume_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchVolumePlan {

	public static final String PLAN_TYPE_SEARCH_VOLUME = "search volume";
	public static final String PLAN_TYPE_KEYWORD_EXPANSION = "keyword expansion";
	public static final String KEYWORD_EXPANSION_TYPE_SEED_KEYWORD = "seed keyword";
	public static final String KEYWORD_EXPANSION_TYPE_SEED_URL = "seed url";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long planId;

	@Column(name = "name")
	private String name;
	
	@Column(name = "is_plan_created")
	private boolean isPlanCreated;
	
	@Column(name = "is_report_ready")
	private boolean isReportReady;
	
	@Column(name = "plan_type")
	private String planType;
	
	@Column(name = "keyword_expansion_type")
	private String keywordExpansionType;
	
	@Column(name = "keyword_plan_network")
	private String keywordPlanNetwork;
	
	@Column(name = "retry_count")
	private long retryCount;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@Column(name = "created_by")
	private String createdBy;
	
	@Column(name = "source")
	private String source;
	
}

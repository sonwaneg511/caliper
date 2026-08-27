package com.caliper.keywordPlanner.entity;

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
@Table(name = "search_volume_keywords")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchVolumeKeywords {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "plan_id")
	private Long planID;
	
	@Column(name = "keyword")
	private String keyword;
	
	@Column(name = "fetch_success")
	private boolean fetchSuccess;
}

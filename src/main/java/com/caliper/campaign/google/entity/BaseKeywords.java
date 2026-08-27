package com.caliper.campaign.google.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "base_keywords")
public class BaseKeywords {
	
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="keyword")
	private String keyword;
	
	@Column(name="source")
	private String source;
	
	@Column(name="source_value")
	private String sourceValue;
	
	@Column(name="search_volume")
	private long searchVolume;
	
	public static final String SOURCE_SUB_INDUSTRY = "sub_industry";
	public static final String SOURCE_URL = "url";
}

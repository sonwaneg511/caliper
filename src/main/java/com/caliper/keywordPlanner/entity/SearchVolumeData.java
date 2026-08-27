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
@Table(name = "search_volume_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchVolumeData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "plan_id")
	private Long planID;
	
	@Column(name = "keyword")
	private String keyword;
	
	@Column(name = "location")
	private String location;
	
	@Column(name = "avg_search_volume")
	private Long avgSearchVolume;
	
	@Column(name = "month_1")
	private Long month1;
	
	@Column(name = "month_2")
	private Long month2;
	
	@Column(name = "month_3")
	private Long month3;
	
	@Column(name = "month_4")
	private Long month4;
	
	@Column(name = "month_5")
	private Long month5;
	
	@Column(name = "month_6")
	private Long month6;
	
	@Column(name = "month_7")
	private Long month7;
	
	@Column(name = "month_8")
	private Long month8;
	
	@Column(name = "month_9")
	private Long month9;
	
	@Column(name = "month_10")
	private Long month10;
	
	@Column(name = "month_11")
	private Long month11;
	
	@Column(name = "month_12")
	private Long month12;
	
	@Column(name = "latest_month")
	private Date latestMonth;
	
	@Column(name = "fetch_success")
	private boolean fetchSuccess;
	
}

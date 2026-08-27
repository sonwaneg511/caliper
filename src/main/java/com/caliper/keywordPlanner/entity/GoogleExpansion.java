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
@Table(name = "google_expansion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleExpansion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "expanded_keyword")
	private String expandedKeyword;

	@Column(name = "plan_id")
	private long planID;
	
	@Column(name = "seed_keyword")
	private String seedKeyword;
	
	@Column(name = "search_volume")
	private long searchVolume;
	
	@Column(name = "inserted_date")
	private Date insertedDate;
	
	@Column(name = "caliper_insertion")
	private boolean caliperInsertion;
	
}

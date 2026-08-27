package com.caliper.bigquery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "project_data")
public class ProjectData {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "project_name", columnDefinition = "TEXT")
	private String projectName;
	
	@Column(name = "data", columnDefinition = "TEXT")
	private String data;
}

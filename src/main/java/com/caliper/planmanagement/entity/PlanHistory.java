package com.caliper.planmanagement.entity;

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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "plan_history")
@Data
public class PlanHistory {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="plan_id")
	private long planId;
	
	@Column(name="client_id")
	private String clientId;
	
	@Column(name="start_date")
	private Date startDate;
	
	@Column(name="end_date")
	private Date endDate;
	
	@Column(name="location_count")
	private long locationCount;
	
	@Column(name="status")
	private String status;
	
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="created_at")
	private Date createdAt;
	
	@Column(name="updated_at")
	private String updatedAt;

	@Column(name="duration_type", length=20)
	private String durationType;

	@Column(name="plan_name")
	private String planName;

	@Column(name="amount")
	private Double amount;
}

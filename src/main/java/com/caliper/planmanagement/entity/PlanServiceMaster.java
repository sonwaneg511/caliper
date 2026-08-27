package com.caliper.planmanagement.entity;

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
@Table(name = "plan_service_master")
@Data
public class PlanServiceMaster {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="service_name")
	private String serviceName;
	
	@Column(name="monthly")
	private Double monthly;
	
	@Column(name="half_yearly")
	private Double halfYearly;
	
	@Column(name="yearly")
	private Double yearly;
	
	@Column(name="description")
	private String description;
	
	@Column(name="service_key")
	private String serviceKey;
}

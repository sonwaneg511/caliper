package com.caliper.reporting.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "gmb_insight_settings")
public class GMBInsightSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "dealer_id")
	private String dealerId;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "last_inserted_date")
	private Date lastInsertedDate;
	
	
}

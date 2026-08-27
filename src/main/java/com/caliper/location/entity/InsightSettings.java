package com.caliper.location.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class InsightSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long id;
	
	@Column(name = "client_id")
	public String clientId;
	
	@Column(name = "dealer_id")
	public String dealerId;
	
	@Column(name = "last_inserted_date")
	public Date lastInsertedDate;
	
	@Column(name = "platform")
	public String platform;
}

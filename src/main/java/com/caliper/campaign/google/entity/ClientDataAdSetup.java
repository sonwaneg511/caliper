package com.caliper.campaign.google.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "client_data_ad_setup")
@Setter
public class ClientDataAdSetup {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="client_id")
	private String client_id;
	
	@Column(name="creative_type")
	private String creativeType;
	
	@Column(name="ad_type")
	private String adType;
	
	@Column(name="value")
	private String value;
}

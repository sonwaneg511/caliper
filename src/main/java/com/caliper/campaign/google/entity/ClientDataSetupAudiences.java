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
@Table(name = "client_data_setup_audiences")
@Setter
public class ClientDataSetupAudiences {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="client_id")
	private String client_id;
	
	@Column(name="google_audience_id")
	private long googleAudienceId;
	
	@Column(name="audience_type")
	private String audienceType;
	
	@Column(name="audience_display_name")
	private String audienceDisplayName;
}

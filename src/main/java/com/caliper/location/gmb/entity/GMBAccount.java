package com.caliper.location.gmb.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "gmb_account",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_gmb_account_account_client",
				columnNames = {"account_id", "client_id"}))
public class GMBAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name="account_id")
	@JsonProperty("account_id")
	private String accountId;

	@Column(name="account_name")
	@JsonProperty("account_name")
	private String accountName;

	@Column(name="client_id")
	@JsonProperty("client_id")
	private String clientId;
		
	@Column(name="status")
	@JsonProperty("status")
	private String status;
	
	@Column(name="last_modified_by")
	@JsonProperty("last_modified_by")
	private String lastModifiedBy;

	@Column(name="last_modified_date")
	@JsonProperty("last_modified_date")
    @Temporal(TemporalType.DATE)
	private Date lastModifiedDate;
	
}

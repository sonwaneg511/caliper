package com.caliper.keywordPlanner.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "google_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleAccount {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	private long id;
	@Column(name = "account_id")
	private String accountId;
	
	@Column(name = "mcc_id")
	private String mccId;
	
	@Column(name = "login_customer_id")
	private long loginCustomerId;
	
	@Column(name = "account_name")
	private String accountName;//client name
	
	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "process_history")
	private boolean processHistory;
	
	@Column(name = "last_modified_by")
	private String lastModifiedBy;
	
	@Column(name = "last_modified_date")
	private Date lastModifiedDate;
	
	@Column(name = "report_last_processed")
	private Date reportLastProcessed;

}

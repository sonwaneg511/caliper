package com.caliper.usermanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name="user_client_loc_mapping")
public class UserClientLocMapping {
	
	//user_client_loc_mapping
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="user_id")
	private String userId;
	
	//make this dealer id
	@Column(name="dealer_id")
	private String dealerId;
	
	@Column(name="client_id")
	private String clientId;
	
	@Column(name="facebook_page_id")
	private Long facebookPageId;
}
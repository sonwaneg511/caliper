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
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "user_role_client_mapping")
public class UserRoleClientMapping {
	//user_role_client_mapping
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="user_id")
	private String userId;
	
	@Column(name="client_id")
	private String clientId;
	
	//make this role
	@Column(name="role")
	private String role;
	
	public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
	
	public static final String ROLE_ADMIN = "ADMIN";
	
	public static final String ROLE_USER = "USER";
 
	
}

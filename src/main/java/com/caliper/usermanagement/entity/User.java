package com.caliper.usermanagement.entity;

import java.time.LocalDateTime;
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
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name="users")
public class User {
	
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="user_id")
	private String userId;
	
	@Column(name="user_name")
	private String userName;
	
	private String password;
	
	@Column(name="client_id")
	private String clientId;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created_date")
	private Date createdDate;
	
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="password_changed_at")
	private LocalDateTime passwordChangedAt;
	
	private String active;
	
	public static final String STATUS_ACTIVE = "ACTIVE";

	public static final String STATUS_INACTIVE = "INACTIVE";

	public static final String STATUS_PENDING = "PENDING";
	
	public static final String USER_PASSWORD = "USER_PASSWORD";

	public static final String FORGOT_PASSWORD = "FORGOT_PASSWORD";
}

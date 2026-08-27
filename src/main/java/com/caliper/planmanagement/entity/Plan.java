package com.caliper.planmanagement.entity;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "plan", indexes = {
    @Index(name = "idx_plan_client_id", columnList = "client_id")
})
@Data
public class Plan {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;

	@Column(name="client_id")
	private String clientId;

	@Column(name="start_date")
	private Date startDate;

	@Column(name="end_date")
	private Date endDate;

	@Column(name="location_count")
	private long locationCount;

	@Column(name="status")
	private String status;

	@Column(name="created_by")
	private String createdBy;

	@Column(name="created_at")
	private Date createdAt;

	@Column(name="updated_at")
	private LocalDateTime updatedAt;

	@Column(name="duration")
	private String duration;

	@Enumerated(EnumType.STRING)
	@Column(name="duration_type", length=20)
	private PlanDurationType durationType;

	@Column(name="razorpay_order_id")
	private String razorpayOrderId;

	@Column(name="plan_name")
	private String planName;

	public static final String STATUS_ACTIVE = "ACTIVE";

	public static final String STATUS_EXPIRED = "EXPIRED";

	public static final String STATUS_INACTIVE = "INACTIVE";

}

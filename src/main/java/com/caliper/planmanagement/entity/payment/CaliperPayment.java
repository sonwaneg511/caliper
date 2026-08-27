package com.caliper.planmanagement.entity.payment;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "caliper_payment",
    uniqueConstraints = @UniqueConstraint(name = "uq_caliper_payment_order_id", columnNames = "order_id"),
    indexes = @Index(name = "idx_caliper_payment_client_id", columnList = "client_id")
)
@Data
@Builder
public class CaliperPayment {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;

	@Column(name="client_id")
	private String clientId;

	@Column(name="payment_id")
	private String paymentId;

	@Column(name="order_id")
	private String orderId;

	@Column(name="amount")
	private double amount;

	@Column(name="status")
	private String status;

	@Column(name="plan_id")
	private long planId;

	@Column(name="campaign_id")
	private long campaignId;

	@Column(name="payment_channel_type")
	private String paymentChannelType;

	@Column(name="payment_method")
	private String paymentMethod;

	@Column(name="transaction_datetime")
	private Date transactionDatetime;

	@Column(name="error_description", length=1024)
	private String errorDescription;

	@Column(name="amount_refunded")
	private String amountRefunded;

	@Column(name="user_id")
	private String userId;

	@Column(name="razorpay_signature", length=512)
	private String razorpaySignature;

	@Column(name="webhook_event_id", unique=true)
	private String webhookEventId;

	@Column(name="duration_type", length=20)
	private String durationType;

	@Column(name="cgst_amount", precision=10, scale=2)
	private BigDecimal cgstAmount;

	@Column(name="sgst_amount", precision=10, scale=2)
	private BigDecimal sgstAmount;

	@Column(name="agency_commission", precision=10, scale=2)
	private BigDecimal agencyCommission;

	public static final String PAYMENT_SUCCESS = "success";
	public static final String PAYMENT_FAILED = "failed";
}

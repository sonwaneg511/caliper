
package com.caliper.planmanagement.entity.payment;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(
    name = "razorpay_order",
    indexes = {
        @Index(name = "idx_rzp_order_razorpay_order_id", columnList = "razorpay_order_id"),
        @Index(name = "idx_rzp_order_client_id", columnList = "client_id")
    }
)
public class RazorpayOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "razorpay_order_id", unique = true, nullable = false)
    private String razorpayOrderId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "location_count")
    private long locationCount;

    @Column(name = "duration_type", length = 20)
    private String durationType;

    @Column(name = "service_keys", length = 1024)
    private String serviceKeys;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "agency_commission_paise")
    private Long agencyCommissionPaise;

    @Column(name = "cgst_paise")
    private Long cgstPaise;

    @Column(name = "sgst_paise")
    private Long sgstPaise;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_EXPIRED = "EXPIRED";
}

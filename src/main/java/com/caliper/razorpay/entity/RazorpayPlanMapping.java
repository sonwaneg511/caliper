package com.caliper.razorpay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "razorpay_plan_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"caliper_service_id", "billing_interval"}))
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayPlanMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to plan_service_master.id */
    @Column(name = "caliper_service_id", nullable = false)
    private Long caliperServiceId;

    /** monthly / yearly */
    @Column(name = "billing_interval", nullable = false)
    private String billingInterval;

    /** Razorpay plan_XXX ID */
    @Column(name = "razorpay_plan_id", nullable = false)
    private String razorpayPlanId;

    /** Amount in paise (INR smallest unit) */
    @Column(name = "amount_paise", nullable = false)
    private Long amountPaise;

    @Column(name = "currency")
    private String currency;

    @Column(name = "created_at")
    private Date createdAt;
}

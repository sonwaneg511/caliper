package com.caliper.razorpay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "razorpay_subscription")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RazorpaySubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "razorpay_subscription_id", unique = true)
    private String razorpaySubscriptionId;

    @Column(name = "razorpay_plan_id")
    private String razorpayPlanId;

    /** created / authenticated / active / halted / cancelled / expired */
    @Column(name = "status")
    private String status;

    @Column(name = "auth_link", columnDefinition = "TEXT")
    private String authLink;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "paid_count")
    private Integer paidCount;

    @Column(name = "current_start")
    private Date currentStart;

    @Column(name = "current_end")
    private Date currentEnd;

    @Column(name = "next_charge_at")
    private Date nextChargeAt;

    @Column(name = "billing_interval")
    private String billingInterval;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    public static final String STATUS_CREATED = "created";
    public static final String STATUS_AUTHENTICATED = "authenticated";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_HALTED = "halted";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_EXPIRED = "expired";
}

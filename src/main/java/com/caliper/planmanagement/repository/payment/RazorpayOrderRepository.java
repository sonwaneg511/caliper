package com.caliper.planmanagement.repository.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.planmanagement.entity.payment.RazorpayOrder;

public interface RazorpayOrderRepository extends JpaRepository<RazorpayOrder, Long> {

    Optional<RazorpayOrder> findByRazorpayOrderId(String razorpayOrderId);

    Optional<RazorpayOrder> findByClientIdAndStatus(String clientId, String status);

    Optional<RazorpayOrder> findByCampaignIdAndStatus(Long campaignId, String status);
}

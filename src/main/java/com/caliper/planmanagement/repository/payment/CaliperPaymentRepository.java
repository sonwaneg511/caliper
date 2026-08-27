package com.caliper.planmanagement.repository.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.planmanagement.entity.payment.CaliperPayment;

public interface CaliperPaymentRepository extends JpaRepository<CaliperPayment, Long> {

    Optional<CaliperPayment> findByOrderId(String orderId);

    Optional<CaliperPayment> findByPlanId(long planId);

    List<CaliperPayment> findByCampaignIdIn(List<Long> campaignIds);
}

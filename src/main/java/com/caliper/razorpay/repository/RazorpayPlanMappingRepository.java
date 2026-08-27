package com.caliper.razorpay.repository;

import com.caliper.razorpay.entity.RazorpayPlanMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RazorpayPlanMappingRepository extends JpaRepository<RazorpayPlanMapping, Long> {

    Optional<RazorpayPlanMapping> findByCaliperServiceIdAndBillingInterval(Long caliperServiceId, String billingInterval);

    Optional<RazorpayPlanMapping> findByAmountPaiseAndBillingInterval(Long amountPaise, String billingInterval);
}

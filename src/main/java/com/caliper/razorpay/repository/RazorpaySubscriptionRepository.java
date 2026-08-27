package com.caliper.razorpay.repository;

import com.caliper.razorpay.entity.RazorpaySubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RazorpaySubscriptionRepository extends JpaRepository<RazorpaySubscription, Long> {

    Optional<RazorpaySubscription> findByClientId(String clientId);

    Optional<RazorpaySubscription> findByRazorpaySubscriptionId(String razorpaySubscriptionId);

    boolean existsByClientIdAndStatusIn(String clientId, java.util.List<String> statuses);
}

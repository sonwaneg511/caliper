package com.caliper.onboarding.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.onboarding.entity.ClientOnboardingState;

@Repository
public interface ClientOnboardingStateRepository extends JpaRepository<ClientOnboardingState, Long> {

    Optional<ClientOnboardingState> findByClientId(String clientId);
}

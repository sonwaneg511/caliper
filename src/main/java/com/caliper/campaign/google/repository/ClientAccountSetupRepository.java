package com.caliper.campaign.google.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.google.entity.ClientAccountSetup;

@Repository
public interface ClientAccountSetupRepository extends JpaRepository<ClientAccountSetup, Long>{

	Optional<ClientAccountSetup> findByClientId(String clientId);
}

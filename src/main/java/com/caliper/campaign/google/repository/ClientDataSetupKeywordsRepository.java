package com.caliper.campaign.google.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.campaign.google.entity.ClientDataSetupKeywords;

public interface ClientDataSetupKeywordsRepository extends JpaRepository<ClientDataSetupKeywords, Long>{

	List<ClientDataSetupKeywords> findByClientIdAndSource(String clientId, String source);
}

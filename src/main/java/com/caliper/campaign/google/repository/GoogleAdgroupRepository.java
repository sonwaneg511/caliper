package com.caliper.campaign.google.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.google.entity.GoogleAdgroup;

@Repository
public interface GoogleAdgroupRepository extends JpaRepository<GoogleAdgroup, Long>{

	List<GoogleAdgroup> findByCampaignId(Long campaignId);
	
	GoogleAdgroup findTopByOrderByIdDesc();
	
	Optional<GoogleAdgroup> findFirstByCampaignId(Long campaignId);
}

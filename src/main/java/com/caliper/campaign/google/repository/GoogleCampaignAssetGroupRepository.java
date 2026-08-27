package com.caliper.campaign.google.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.google.entity.GoogleCampaignAssetGroup;

@Repository
public interface GoogleCampaignAssetGroupRepository extends JpaRepository<GoogleCampaignAssetGroup, Long>{
	
	GoogleCampaignAssetGroup findTopByOrderByIdDesc();

	Optional<GoogleCampaignAssetGroup> findByCampaignId(long campaignId);
	
	List<GoogleCampaignAssetGroup> findAllByCampaignId(long campaignId);
}

package com.caliper.campaign.google.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.google.entity.GoogleCampaignGeoDetails;

@Repository
public interface GoogleCampaignGeoDetailsRepository extends JpaRepository<GoogleCampaignGeoDetails, Long>{
	public List<GoogleCampaignGeoDetails> findAllGoogleCampaignGeoDetailsByCampaignId(long campaignId);
}

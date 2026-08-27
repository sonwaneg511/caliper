package com.caliper.campaign.google.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.google.entity.GoogleCampaignAssets;

@Repository
public interface GoogleCampaignAssetsRepository extends JpaRepository<GoogleCampaignAssets, Long>{

	 void deleteByAssetGroupIdAndType(Long assetGroupId, String type);
	 
	 List<GoogleCampaignAssets> findByAssetGroupId(long assetGroupId);
}

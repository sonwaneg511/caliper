package com.caliper.campaign.facebook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.campaign.facebook.entity.MetaAdImageAsset;

public interface MetaAdImageAssetRepository extends JpaRepository<MetaAdImageAsset, Long> {

    List<MetaAdImageAsset> findByCampaignIdOrderByAssetOrderAsc(long campaignId);
}

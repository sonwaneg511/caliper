package com.caliper.campaign.facebook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.facebook.entity.MetaAdImage;

@Repository
public interface MetaAdImageRepository extends JpaRepository<MetaAdImage, Long> {

    List<MetaAdImage> findByCampaignId(long campaignId);

    List<MetaAdImage> findByClientId(String clientId);
}

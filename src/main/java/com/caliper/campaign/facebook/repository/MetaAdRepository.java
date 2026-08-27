package com.caliper.campaign.facebook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.facebook.entity.MetaAd;

@Repository
public interface MetaAdRepository extends JpaRepository<MetaAd, Long> {

    List<MetaAd> findByAdSetId(long adSetId);

    List<MetaAd> findByCampaignId(long campaignId);

    Optional<MetaAd> findFirstByAdSetId(long adSetId);
}

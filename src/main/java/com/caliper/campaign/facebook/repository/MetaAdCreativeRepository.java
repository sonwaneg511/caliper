package com.caliper.campaign.facebook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.facebook.entity.MetaAdCreative;

@Repository
public interface MetaAdCreativeRepository extends JpaRepository<MetaAdCreative, Long> {

    List<MetaAdCreative> findByCampaignId(long campaignId);

    Optional<MetaAdCreative> findFirstByCampaignId(long campaignId);
}

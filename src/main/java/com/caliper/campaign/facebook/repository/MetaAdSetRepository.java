package com.caliper.campaign.facebook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.facebook.entity.MetaAdSet;

@Repository
public interface MetaAdSetRepository extends JpaRepository<MetaAdSet, Long> {

    List<MetaAdSet> findByCampaignId(long campaignId);

    Optional<MetaAdSet> findFirstByCampaignId(long campaignId);
}

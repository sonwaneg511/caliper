package com.caliper.campaign.facebook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.facebook.entity.MetaLead;

@Repository
public interface MetaLeadRepository extends JpaRepository<MetaLead, Long> {

    List<MetaLead> findByClientId(String clientId);

    List<MetaLead> findByMetaCampaignId(String metaCampaignId);

    Optional<MetaLead> findByMetaLeadId(String metaLeadId);
}

package com.caliper.campaign.facebook.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.facebook.entity.MetaCampaign;

@Repository
public interface MetaCampaignRepository extends JpaRepository<MetaCampaign, Long> {

    List<MetaCampaign> findAllByStatus(String status);

    List<MetaCampaign> findByClientIdAndDealerIdIn(String clientId, List<String> dealerIds);

    Page<MetaCampaign> findByClientIdAndDealerIdInOrderByIdDesc(String clientId, List<String> dealerIds, Pageable pageable);

    List<MetaCampaign> findByClientId(String clientId);
}

package com.caliper.campaign.facebook.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.caliper.campaign.facebook.entity.MetaAdCarouselCard;

@Repository
public interface MetaAdCarouselCardRepository extends JpaRepository<MetaAdCarouselCard, Long> {
    List<MetaAdCarouselCard> findByCampaignIdOrderByCardOrderAsc(long campaignId);
    List<MetaAdCarouselCard> findByCreativeIdOrderByCardOrderAsc(long creativeId);
}

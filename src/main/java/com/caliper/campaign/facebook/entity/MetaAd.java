package com.caliper.campaign.facebook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meta_ad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaAd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "adset_id")
    private long adSetId;

    @Column(name = "campaign_id")
    private long campaignId;

    @Column(name = "ad_name")
    private String adName;

    @Column(name = "meta_ad_id")
    private String metaAdId;

    @Column(name = "creative_id")
    private long creativeId;

    @Column(name = "status")
    private String status;
}

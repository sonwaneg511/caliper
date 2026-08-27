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
@Table(name = "meta_ad_image_asset")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaAdImageAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "campaign_id")
    private long campaignId;

    @Column(name = "creative_id")
    private long creativeId;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "image_hash")
    private String imageHash;

    // Aspect ratio label supplied by the frontend, e.g. "1:1", "1.91:1", "9:16", "4:5"
    @Column(name = "ratio")
    private String ratio;

    @Column(name = "asset_order")
    private int assetOrder;
}

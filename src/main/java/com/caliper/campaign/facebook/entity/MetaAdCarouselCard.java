package com.caliper.campaign.facebook.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meta_ad_carousel_card")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MetaAdCarouselCard {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "campaign_id")   private long   campaignId;
    @Column(name = "creative_id")   private long   creativeId;
    @Column(name = "card_order")    private int    cardOrder;
    @Column(name = "headline")      private String headline;
    @Column(name = "description")   private String description;
    @Column(name = "image_url", columnDefinition = "TEXT")  private String imageUrl;
    @Column(name = "image_hash")    private String imageHash;
    @Column(name = "link_url")      private String linkUrl;
    @Column(name = "video_url", columnDefinition = "TEXT")  private String videoUrl;
    @Column(name = "video_id")      private String videoId;

    public static final String STATUS_PENDING  = "PENDING";
    public static final String STATUS_UPLOADED = "UPLOADED";
}

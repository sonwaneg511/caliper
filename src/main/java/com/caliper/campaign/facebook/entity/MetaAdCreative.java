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
@Table(name = "meta_ad_creative")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaAdCreative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "campaign_id")
    private long campaignId;

    @Column(name = "meta_creative_id")
    private String metaCreativeId;

    @Column(name = "name")
    private String name;

    @Column(name = "page_id")
    private String pageId;

    @Column(name = "headline")
    private String headline;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "description")
    private String description;

    @Column(name = "call_to_action_type")
    private String callToActionType;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "image_hash")
    private String imageHash;

    @Column(name = "video_id")
    private String videoId;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "thumbnail_hash")
    private String thumbnailHash;

    @Column(name = "creative_type")
    private String creativeType;

    @Column(name = "degrees_of_freedom")
    private boolean degreesOfFreedom;

    public static final String CTA_LEARN_MORE  = "LEARN_MORE";
    public static final String CTA_SIGN_UP     = "SIGN_UP";
    public static final String CTA_CONTACT_US  = "CONTACT_US";
    public static final String CTA_GET_QUOTE   = "GET_QUOTE";
    public static final String CTA_APPLY_NOW   = "APPLY_NOW";

    public static final String CREATIVE_TYPE_IMAGE    = "IMAGE";
    public static final String CREATIVE_TYPE_VIDEO    = "VIDEO";
    public static final String CREATIVE_TYPE_CAROUSEL = "CAROUSEL";
}

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
@Table(name = "meta_ad_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaAdImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "campaign_id")
    private long campaignId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "image_hash")
    private String imageHash;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "status")
    private String status;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_UPLOADED = "UPLOADED";
    public static final String STATUS_ERROR = "ERROR";
}

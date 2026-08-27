package com.caliper.campaign.facebook.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meta_lead")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "meta_lead_id", unique = true)
    private String metaLeadId;

    @Column(name = "meta_ad_id")
    private String metaAdId;

    @Column(name = "form_id")
    private String formId;

    @Column(name = "meta_campaign_id")
    private String metaCampaignId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "dealer_id")
    private String dealerId;

    @Column(name = "lead_data", columnDefinition = "TEXT")
    private String leadData;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time")
    private Date createdTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "received_at")
    private Date receivedAt;
}

package com.caliper.campaign.facebook.entity;

import java.math.BigDecimal;
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
@Table(name = "meta_campaign")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "dealer_id")
    private String dealerId;

    @Column(name = "campaign_name")
    private String campaignName;

    @Column(name = "meta_campaign_id")
    private String metaCampaignId;

    @Column(name = "objective")
    private String objective;

    @Column(name = "daily_budget")
    private BigDecimal dailyBudget;

    @Column(name = "total_budget")
    private BigDecimal totalBudget;

    @Column(name = "budget_type")
    private String budgetType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "stop_time")
    private Date stopTime;

    @Column(name = "status")
    private String status;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "coe_comment", columnDefinition = "TEXT")
    private String coeComment;

    @Column(name = "error_comment", columnDefinition = "TEXT")
    private String errorComment;

    @Column(name = "client_comment")
    private String clientComment;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "destination_type")
    private String destinationType;

    public static final String STATUS_DRAFT = "Draft";
    public static final String STATUS_PENDING_DEPLOYMENT = "Pending Deployment";
    public static final String STATUS_DEPLOYED = "Deployed";
    public static final String STATUS_ERROR = "Error";
    public static final String STATUS_PAUSED = "Paused";

    public static final String OBJECTIVE_AWARENESS = "OUTCOME_AWARENESS";
    public static final String OBJECTIVE_TRAFFIC = "OUTCOME_TRAFFIC";
    public static final String OBJECTIVE_LEADS = "OUTCOME_LEADS";
    public static final String OBJECTIVE_ENGAGEMENT = "OUTCOME_ENGAGEMENT";
    public static final String OBJECTIVE_SALES = "OUTCOME_SALES";

    public static final String BUDGET_TYPE_DAILY = "DAILY";
    public static final String BUDGET_TYPE_LIFETIME = "LIFETIME";

    public static final String ROLE_CLIENT = "client";
    public static final String ROLE_HUB_USER = "hub_user";
}

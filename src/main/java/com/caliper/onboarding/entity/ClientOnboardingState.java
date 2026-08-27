package com.caliper.onboarding.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "client_onboarding_state", indexes = {
    @Index(name = "idx_cos_client_id", columnList = "client_id", unique = true)
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientOnboardingState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", unique = true, nullable = false, length = 500)
    private String clientId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 30)
    private OnboardingStep currentStep;

    @Column(name = "requires_social", nullable = false)
    private boolean requiresSocial;

    @Column(name = "requires_campaign", nullable = false)
    private boolean requiresCampaign;

    @Convert(converter = ConnectionStatusConverter.class)
    @Column(name = "gmb_status", length = 20)
    private ConnectionStatus gmbStatus;

    @Convert(converter = ConnectionStatusConverter.class)
    @Column(name = "meta_status", length = 20)
    private ConnectionStatus metaStatus;
    
    @Convert(converter = ConnectionStatusConverter.class)
    @Column(name = "is_gmb_location_selected", length = 20)
    private ConnectionStatus locationStatus;

    @Convert(converter = ConnectionStatusConverter.class)
    @Column(name = "campaign_status", length = 20)
    private ConnectionStatus campaignStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}

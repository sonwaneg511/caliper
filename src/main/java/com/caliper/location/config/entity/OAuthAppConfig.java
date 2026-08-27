package com.caliper.location.config.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth_app_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthAppConfig {

    @Id
    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "app_id", nullable = false, length = 500)
    private String appId;

    @Column(name = "app_secret", nullable = false, length = 500)
    private String appSecret;
}

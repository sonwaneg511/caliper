package com.caliper.location.gmb.entity;

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
@Table(name = "gmb_oauth_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GMBOAuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "client_id", unique = true, nullable = false, length = 500)
    private String clientId;

    @Column(name = "gcp_client_id", nullable = false, length = 500)
    private String gcpClientId;

    @Column(name = "gcp_client_secret", nullable = false, length = 500)
    private String gcpClientSecret;

    @Column(name = "access_token", nullable = false, length = 2000)
    private String accessToken;

    @Column(name = "refresh_token", length = 2000)
    private String refreshToken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "token_expiry")
    private Date tokenExpiry;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;
}

package com.caliper.location.facebook.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "facebook_account")
public class FacebookAccount {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "client_id")
    private String clientId;

    // null = shared across all dealers for this client; non-null = dealer-specific
    @Column(name = "dealer_id")
    private String dealerId;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "pixel_id")
    private Long pixelId;

    @Column(name = "offline_event_set_id")
    private Long offlineEventSetId;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;
}

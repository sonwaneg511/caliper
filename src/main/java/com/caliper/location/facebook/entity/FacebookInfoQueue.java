package com.caliper.location.facebook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "facebook_info_queue")
public class FacebookInfoQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "client_id")
    private String clientId;
    
    @Column(name = "facebook_page_id")
    private String facebookPageId;
    
    @Column(name = "dealer_id")
    private String dealerId;

    @Column(length = 10000)
    private String requestJson;

    @Column(name = "status")
    private String status;
    
    public static final String STATUS_SUBMIT = "submit";
    public static final String STATUS_DEPLOYED = "deployed";
    public static final String STATUS_ERROR = "error";

    public static final String SOURCE_FACEBOOK = "Facebook";
}

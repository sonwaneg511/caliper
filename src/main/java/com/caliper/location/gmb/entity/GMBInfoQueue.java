package com.caliper.location.gmb.entity;

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
@Table(name = "gmb_info_queue")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GMBInfoQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "dealer_id")
    private String dealerId;

    @Column(length = 10000) 
    private String requestJson;

    @Column(name = "status")
    private String status;

    // Status constants
    public static final String STATUS_SUBMIT = "submit";
    public static final String STATUS_DEPLOYED = "deployed";
    public static final String STATUS_ERROR = "error";

    // Source constants
    public static final String SOURCE_GMB = "GMB";

    // Place action types
    public static final String PLACE_ACTION_TYPE_APPOINTMENT = "APPOINTMENT";
}

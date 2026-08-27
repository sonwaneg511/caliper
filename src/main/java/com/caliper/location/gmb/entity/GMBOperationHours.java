package com.caliper.location.gmb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "gmb_operation_hours")
public class GMBOperationHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "client_id")
    @JsonProperty("client_id")
    private String clientId;

    @Column(name = "dealer_id")
    @JsonProperty("dealer_id")
    private String dealerId;

    @Column(name = "gmb_location_id")
    @JsonProperty("gmb_location_id")
    private String gmbLocationId;

    @Column(name = "monday_open_time")
    @JsonProperty("monday_open_time")
    private String mondayOpenTime;

    @Column(name = "monday_close_time")
    @JsonProperty("monday_close_time")
    private String mondayCloseTime;

    @Column(name = "tuesday_open_time")
    @JsonProperty("tuesday_open_time")
    private String tuesdayOpenTime;

    @Column(name = "tuesday_close_time")
    @JsonProperty("tuesday_close_time")
    private String tuesdayCloseTime;

    @Column(name = "wednesday_open_time")
    @JsonProperty("wednesday_open_time")
    private String wednesdayOpenTime;

    @Column(name = "wednesday_close_time")
    @JsonProperty("wednesday_close_time")
    private String wednesdayCloseTime;

    @Column(name = "thursday_open_time")
    @JsonProperty("thursday_open_time")
    private String thursdayOpenTime;

    @Column(name = "thursday_close_time")
    @JsonProperty("thursday_close_time")
    private String thursdayCloseTime;

    @Column(name = "friday_open_time")
    @JsonProperty("friday_open_time")
    private String fridayOpenTime;

    @Column(name = "friday_close_time")
    @JsonProperty("friday_close_time")
    private String fridayCloseTime;

    @Column(name = "saturday_open_time")
    @JsonProperty("saturday_open_time")
    private String saturdayOpenTime;

    @Column(name = "saturday_close_time")
    @JsonProperty("saturday_close_time")
    private String saturdayCloseTime;

    @Column(name = "sunday_open_time")
    @JsonProperty("sunday_open_time")
    private String sundayOpenTime;

    @Column(name = "sunday_close_time")
    @JsonProperty("sunday_close_time")
    private String sundayCloseTime;

    @OneToOne(mappedBy = "gmbOperationHours")
    private GMBLocation gmbLocation;

    public static final String WORKING_SUNDAY = "SUNDAY";
    public static final String WORKING_MONDAY = "MONDAY";
    public static final String WORKING_TUESDAY = "TUESDAY";
    public static final String WORKING_WEDNESDAY = "WEDNESDAY";
    public static final String WORKING_THURSDAY = "THURSDAY";
    public static final String WORKING_FRIDAY = "FRIDAY";
    public static final String WORKING_SATURDAY = "SATURDAY";
}

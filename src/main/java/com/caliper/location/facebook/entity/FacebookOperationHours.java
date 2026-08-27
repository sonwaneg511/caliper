package com.caliper.location.facebook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data
@Table(name = "facebook_operation_hours")
public class FacebookOperationHours {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "client_id")
    private String clientId;
    
    @Column(name = "dealer_id")
    private String dealerId;
    
    @Column(name = "fb_page_id")
    private String fbPageId;

    @Column(name = "monday_open_time")
    private String mondayOpenTime;

    @Column(name = "monday_close_time")
    private String mondayCloseTime;

    @Column(name = "tuesday_open_time")
    private String tuesdayOpenTime;

    @Column(name = "tuesday_close_time")
    private String tuesdayCloseTime;

    @Column(name = "wednesday_open_time")
    private String wednesdayOpenTime;

    @Column(name = "wednesday_close_time")
    private String wednesdayCloseTime;

    @Column(name = "thursday_open_time")
    private String thursdayOpenTime;

    @Column(name = "thursday_close_time")
    private String thursdayCloseTime;

    @Column(name = "friday_open_time")
    private String fridayOpenTime;

    @Column(name = "friday_close_time")
    private String fridayCloseTime;

    @Column(name = "saturday_open_time")
    private String saturdayOpenTime;

    @Column(name = "saturday_close_time")
    private String saturdayCloseTime;

    @Column(name = "sunday_open_time")
    private String sundayOpenTime;

    @Column(name = "sunday_close_time")
    private String sundayCloseTime;

    @OneToOne(mappedBy = "facebookOperationHours")
    private FacebookLocation facebookLocation;
    
    public static final String WORKING_SUNDAY = "SUNDAY";
    public static final String WORKING_MONDAY = "MONDAY";
    public static final String WORKING_TUESDAY = "TUESDAY";
    public static final String WORKING_WEDNESDAY = "WEDNESDAY";
    public static final String WORKING_THURSDAY = "THURSDAY";
    public static final String WORKING_FRIDAY = "FRIDAY";
    public static final String WORKING_SATURDAY = "SATURDAY";
}

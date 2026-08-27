package com.caliper.location.gmb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "gmb_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMBSettings {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    /* ===== Constants (safe to keep) ===== */

    public static final String GMB_SETTING_REVIEWS_LAST_INSERTED_DATE = "reviews last inserted date";
    public static final String GMB_SETTING_INSIGHTS_LAST_INSERTED_DATE = "insights last inserted date";
    public static final String GMB_SETTING_DEALER_REVIEW_LAST_INSERTED_DATE = "dealer review last inserted date";
    public static final String GMB_SETTING_DEALER_REVIEW_AUTO_RESPONSE_DATE = "dealer review auto response date";

    public static final String GMB_TICKETING_MAIL_NAMES = "ticketing_mail_names";
    public static final String GMB_TICKETING_MAIL_IDS = "ticketing_mail_ids";
    public static final String GMB_SETTING_DEALER_MAX_STAR_RATING = "dealer max star rating";
    public static final String REVIEW_TAGS = "review tags";
    public static final String REVIEW_REPLY = "review reply";

    public static final String EMAIL_FROM = "email_from";
    public static final String EMAIL_HOST = "email_host";
    public static final String EMAIL_USERNAME = "email_username";
    public static final String EMAIL_PASSWORD = "email_password";
    public static final String QR_LOGO_IMAGE = "qr_logo_image";
}


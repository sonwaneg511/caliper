package com.caliper.images.entity;

import java.util.Date;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location_image", indexes = {
        @Index(name = "idx_loc_img_client_id", columnList = "client_id"),
        @Index(name = "idx_loc_img_status", columnList = "status"),
        @Index(name = "idx_loc_img_created_date", columnList = "created_date"),
        @Index(name = "idx_loc_img_category", columnList = "image_category"),
        @Index(name = "idx_loc_img_platform", columnList = "platform")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "client_id", length = 255)
    private String clientId;

    @Column(name = "image_url", length = 2000)
    private String imageUrl;

    @Column(name = "image_category", length = 50)
    private String imageCategory;

    @Column(name = "image_format", length = 50)
    private String imageFormat;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "label", length = 100)
    private String label;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "platform", nullable = false, length = 30)
    private String platform;

    public static final String STATUS_SUBMIT = "submit";
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_DEPLOYED = "deployed";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_SUPERSEDED = "superseded";

    public static final String IMAGE_FORMAT_PHOTO = "PHOTO";
    public static final String IMAGE_FORMAT_VIDEO = "VIDEO";

    public static final String PLATFORM_GMB = "GMB";
    public static final String PLATFORM_FACEBOOK = "FACEBOOK";

    public static final String CATEGORY_PROFILE = "PROFILE";
    public static final String CATEGORY_COVER = "COVER";
    public static final String CATEGORY_LOGO = "LOGO";
    public static final String CATEGORY_TIMELINE = "TIMELINE";
    public static final String CATEGORY_EXTERIOR = "EXTERIOR";
    public static final String CATEGORY_INTERIOR = "INTERIOR";
    public static final String CATEGORY_PRODUCT = "PRODUCT";
    public static final String CATEGORY_AT_WORK = "AT_WORK";
    public static final String CATEGORY_FOOD_AND_DRINK = "FOOD_AND_DRINK";
    public static final String CATEGORY_MENU = "MENU";
    public static final String CATEGORY_COMMON_AREA = "COMMON_AREA";
    public static final String CATEGORY_ROOMS = "ROOMS";
    public static final String CATEGORY_TEAMS = "TEAMS";
    public static final String CATEGORY_ADDITIONAL = "ADDITIONAL";

    // GMB only ever has one active logo, cover, and profile photo per location.
    public static final Set<String> SINGLETON_CATEGORIES = Set.of(CATEGORY_LOGO, CATEGORY_COVER, CATEGORY_PROFILE);
}

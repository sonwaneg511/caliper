package com.caliper.campaign.facebook.entity;

import java.util.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meta_geo_location",
       uniqueConstraints = @UniqueConstraint(columnNames = {"meta_key", "location_type"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MetaGeoLocation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "meta_key", nullable = false)
    private String metaKey;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location_type", nullable = false)
    private String locationType;      // city | region | zip

    @Column(name = "country_code")
    private String countryCode;       // e.g. IN

    @Column(name = "region_name")
    private String regionName;        // parent region name (populated for city rows)

    @Column(name = "synced_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date syncedAt;

    public static final String TYPE_CITY   = "city";
    public static final String TYPE_REGION = "region";
    public static final String TYPE_ZIP    = "zip";
    public static final String TYPE_COUNTRY =  "country";
}

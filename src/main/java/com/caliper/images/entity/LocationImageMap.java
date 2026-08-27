package com.caliper.images.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location_image_map", indexes = {
        @Index(name = "idx_loc_img_map_dealer", columnList = "dealer_id"),
        @Index(name = "idx_loc_img_map_client", columnList = "client_id"),
        @Index(name = "idx_loc_img_map_status", columnList = "status"),
        @Index(name = "idx_loc_img_map_created_date", columnList = "created_date")
})
@IdClass(LocationImageMapId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationImageMap {

    @Id
    @Column(name = "image_id")
    private Long imageId;

    @Id
    @Column(name = "dealer_id", length = 200)
    private String dealerId;

    @Column(name = "client_id", length = 255)
    private String clientId;

    @Column(name = "status", length = 30)
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdDate;

    @Column(name = "console_image_id", length = 500)
    private String consoleImageId;

    @Column(name = "version_marker", length = 500)
    private String versionMarker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", referencedColumnName = "image_id", insertable = false, updatable = false)
    private LocationImage locationImage;
}

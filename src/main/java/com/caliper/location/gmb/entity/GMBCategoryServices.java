package com.caliper.location.gmb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

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
@Table(name = "gmb_category_services")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GMBCategoryServices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonProperty("id")
    private Long id;

    @Column(name = "category_id")
    @JsonProperty("category_id")
    private String categoryId;

    @Column(name = "service_id")
    @JsonProperty("service_id")
    private String serviceId;

    @Column(name = "display_name")
    @JsonProperty("display_name")
    private String displayName;
}

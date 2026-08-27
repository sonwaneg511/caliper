package com.caliper.location.gmb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gmb_category")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GMBCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonProperty("id")
    private long id;

    @Column(name = "category_id")
    @JsonProperty("category_id")
    private String categoryId;

    @Column(name = "display_name")
    @JsonProperty("display_name")
    private String displayName;
}

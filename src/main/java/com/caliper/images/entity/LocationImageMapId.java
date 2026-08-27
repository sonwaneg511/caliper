package com.caliper.images.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationImageMapId implements Serializable {

    private Long imageId;
    private String dealerId;
}

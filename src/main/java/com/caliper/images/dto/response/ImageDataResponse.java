package com.caliper.images.dto.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDataResponse {

    @JsonProperty("image_id")
    private Long imageId;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("image_category")
    private String imageCategory;

    @JsonProperty("image_format")
    private String imageFormat;

    private String description;

    private String label;

    private String status;

    @JsonProperty("created_date")
    private Date createdDate;

    private int dealers;
}

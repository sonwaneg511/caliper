package com.caliper.campaign.facebook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ImageAssetDto {

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("ratio")
    private String ratio; // e.g. "1:1", "1.91:1", "9:16", "4:5"
}

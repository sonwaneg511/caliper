package com.caliper.campaign.facebook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CarouselCardDto {

    @NotBlank
    @JsonProperty("headline")
    private String headline;

    @JsonProperty("description")
    private String description;

    @NotBlank
    @JsonProperty("image_url")
    private String imageUrl;

    @NotBlank
    @JsonProperty("link_url")
    private String linkUrl;
}

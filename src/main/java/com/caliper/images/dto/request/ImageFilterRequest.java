package com.caliper.images.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageFilterRequest {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("image_category")
    private String imageCategory;

    private String status;

    @JsonProperty("page_no")
    private int pageNo;
}

package com.caliper.images.dto.request;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UploadImageRequest {

    @JsonProperty("dealer_id")
    private List<String> dealerId;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("image_category")
    private String imageCategory;

    @JsonProperty("image_format")
    private String imageFormat;

    @JsonProperty("image_url")
    private String imageUrl;
    
    private String description;

    private String label;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    private String status;

    private String comment;

    private String platform;
}

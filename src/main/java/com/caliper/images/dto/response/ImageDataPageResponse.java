package com.caliper.images.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDataPageResponse {

    @JsonProperty("image_data_list")
    private List<ImageDataResponse> imageDataResponseList;

    @JsonProperty("total_pages")
    private int totalNoOfPages;

    @JsonProperty("total_records")
    private long totalNoOfRecords;
}

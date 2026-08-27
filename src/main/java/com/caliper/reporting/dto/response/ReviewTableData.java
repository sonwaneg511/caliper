package com.caliper.reporting.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class ReviewTableData {

    @JsonProperty("data")
    public List<ReviewTableRowData> data;

    @JsonProperty("total_no_pages")
    public int totalNoPages;
}

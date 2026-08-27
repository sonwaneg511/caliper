package com.caliper.reporting.dto.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class ReviewTableRowData {

    @JsonProperty("dealer_id")
    public String dealerId;

    @JsonProperty("dealer_name")
    public String dealerName;

    @JsonProperty("reviewer")
    public String reviewer;

    @JsonProperty("city")
    public String city;

    @JsonProperty("comment")
    public String comment;

    @JsonProperty("reply")
    public String reply;

    @JsonProperty("rating")
    public long rating;

    @JsonProperty("date")
    public Date date;

    @JsonProperty("reply_date")
    public Date replyDate;
}

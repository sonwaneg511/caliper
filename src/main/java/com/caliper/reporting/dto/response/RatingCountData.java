package com.caliper.reporting.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RatingCountData {

    private int stars;

    private double percentage;
}
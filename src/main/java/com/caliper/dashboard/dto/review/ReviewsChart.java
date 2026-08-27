package com.caliper.dashboard.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewsChart {

    private String month;         
    private long totalReviews;     
    private double totalRating;  
}

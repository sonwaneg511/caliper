package com.caliper.dashboard.dto.review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewLocation {

    private String locationId;
    private String locationName;
    private long ratings;     
    private double averageRating; 
}

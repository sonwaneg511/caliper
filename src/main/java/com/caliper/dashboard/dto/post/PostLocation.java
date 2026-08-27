package com.caliper.dashboard.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostLocation {

	private String id;
    private String locationName;
    private double averagePosts;
}

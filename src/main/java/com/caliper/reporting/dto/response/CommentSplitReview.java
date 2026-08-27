package com.caliper.reporting.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentSplitReview {

    private String name;

    private long value;
}
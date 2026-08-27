package com.caliper.post.repository;

public interface PostMetricsProjection {

    Long getPostId();
    Long getDealerCount();
    Long getLikes();
    Long getComments();
    Long getShares();
}
package com.caliper.dashboard.dto.review;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AggregatedReviewDTO {

    private String dealerId;
    private long totalReviews;
    private long ratingSum;

    private long five;
    private long four;
    private long three;
    private long two;
    private long one;

    private long positive;
    private long neutral;
    private long negative;

    private int year;
    private int month;

    public AggregatedReviewDTO(
            String dealerId,
            long totalReviews,
            long ratingSum,
            long five,
            long four,
            long three,
            long two,
            long one,
            long positive,
            long neutral,
            long negative,
            int year,
            int month
    ) {
        this.dealerId = dealerId;
        this.totalReviews = totalReviews;
        this.ratingSum = ratingSum;
        this.five = five;
        this.four = four;
        this.three = three;
        this.two = two;
        this.one = one;
        this.positive = positive;
        this.neutral = neutral;
        this.negative = negative;
        this.year = year;
        this.month = month;
    }

    // ✅ Derived value (safe)
    public double getAvgRating() {
        return totalReviews == 0 ? 0.0 : (double) ratingSum / totalReviews;
    }
}


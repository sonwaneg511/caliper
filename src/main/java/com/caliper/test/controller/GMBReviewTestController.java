package com.caliper.test.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.test.entity.GMBReviewTest;
import com.caliper.test.service.GMBReviewTestService;
import com.google.cloud.bigquery.JobException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/gmb-review")
@Validated
public class GMBReviewTestController {

    private final GMBReviewTestService gmbService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public GMBReviewTestController(GMBReviewTestService gmbService) {
        this.gmbService = gmbService;
    }

    @PostMapping("/insert/{clientName}")
    public ResponseEntity<String> insertReviews(@PathVariable String clientName,
                                                @Valid @RequestBody List<GMBReviewTest> reviews) throws IOException, SQLException {
        System.out.println("reviews :: "+reviews);
        gmbService.insertReviews(clientName, reviews);
        return ResponseEntity.ok("Inserted " + reviews.size() + " reviews.");
    }

    @DeleteMapping("/delete/{clientName}")
    public ResponseEntity<String> deleteReviews(@PathVariable String clientName,
                                                @RequestParam String dateTime) throws JobException, IOException, InterruptedException, SQLException {
        LocalDateTime dt = LocalDateTime.parse(dateTime, formatter);
        gmbService.deleteReviews(clientName, dt.format(formatter));
        return ResponseEntity.ok("Deleted reviews from " + dateTime);
    }
}
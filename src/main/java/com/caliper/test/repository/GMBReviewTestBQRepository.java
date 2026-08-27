package com.caliper.test.repository;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.caliper.test.entity.GMBReviewTest;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableId;

@Repository
public class GMBReviewTestBQRepository {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String PROJECT_ID = "ia-reports-123456";

    public void insertBatch(BigQuery bigQuery, String clientName, List<GMBReviewTest> reviews) {
        TableId tableId = TableId.of(PROJECT_ID, clientName, GMBReviewTest.SQL_TABLE);
        List<GMBReviewTest> batch = new ArrayList<>();
        int count = 0;

        for (GMBReviewTest review : reviews) {
            batch.add(review);
            count++;
            if (count % 1000 == 0) {
                executeBatch(bigQuery, tableId, batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) executeBatch(bigQuery, tableId, batch);
    }

    private void executeBatch(BigQuery bigQuery, TableId tableId, List<GMBReviewTest> batch) {
        InsertAllRequest.Builder builder = InsertAllRequest.newBuilder(tableId);

        for (GMBReviewTest review : batch) builder.addRow(mapRow(review));

        InsertAllResponse response = bigQuery.insertAll(builder.build());

        if (response.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
                errors.append(entry.getValue()).append(" | ");
            }
            throw new RuntimeException(errors.toString());
        }
    }

    public void delete(BigQuery bigQuery, String clientName, String createdTime) throws InterruptedException, JobException {
        String query = String.format("DELETE FROM `%s.%s.%s` WHERE created_time >= '%s'",
                PROJECT_ID, clientName, GMBReviewTest.SQL_TABLE, createdTime);
        bigQuery.query(QueryJobConfiguration.newBuilder(query).build());
    }

    private Map<String, Object> mapRow(GMBReviewTest review) {
        Map<String, Object> row = new HashMap<>();
        row.put(GMBReviewTest.SQL_COLUMN_REVIEW_ID, review.getReviewId());
        row.put(GMBReviewTest.SQL_COLUMN_LOCATION_ID, review.getLocationId());
        row.put(GMBReviewTest.SQL_COLUMN_DEALER_ID, review.getDealerId());
        row.put(GMBReviewTest.SQL_COLUMN_REVIEW_NAME, review.getReviewName());
        row.put(GMBReviewTest.SQL_COLUMN_STAR_RATING, review.getStarRating());
        row.put(GMBReviewTest.SQL_COLUMN_COMMENT, review.getComment());
        row.put(GMBReviewTest.SQL_COLUMN_REPLY_COMMENT, review.getReplyComment());
        row.put(GMBReviewTest.SQL_COLUMN_REVIEWER, review.getReviewer());
        row.put(GMBReviewTest.SQL_COLUMN_REPLY_STATUS, review.getReplyStatus());
        row.put(GMBReviewTest.SQL_COLUMN_REVIEW_STATUS, review.isReviewStatus());
        row.put(GMBReviewTest.SQL_COLUMN_CREATED_TIME, review.getCreatedTime());
        return row;
    }
}
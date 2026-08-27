package com.caliper.test.service;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.bigquery.service.BigQueryConfig;
import com.caliper.test.entity.GMBReviewTest;
import com.caliper.test.repository.GMBReviewTestBQRepository;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.JobException;

@Service
public class GMBReviewTestService {

	@Autowired
    private GMBReviewTestBQRepository gmbDao;
	@Autowired
    private BigQueryConfig bigQueryDao;
	
    public void insertReviews(String clientName, List<GMBReviewTest> reviews) throws IOException, SQLException {
        BigQuery bq = bigQueryDao.getBigQueryDao();
        gmbDao.insertBatch(bq, clientName, reviews);
    }

    public void deleteReviews(String clientName, String dateTimeStr) throws IOException, InterruptedException, JobException, SQLException {
        BigQuery bq = bigQueryDao.getBigQueryDao();
        gmbDao.delete(bq, clientName, dateTimeStr);
    }
}

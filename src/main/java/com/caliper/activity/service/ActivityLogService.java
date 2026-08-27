package com.caliper.activity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.activity.entity.ActivityLog;
import com.caliper.activity.repository.ActivityLogRepository;

@Service
public class ActivityLogService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogService.class);

    @Autowired
    private ActivityLogRepository activityLogRepository;

    public void save(ActivityLog activityLog) {
        try {
            activityLogRepository.save(activityLog);
        } catch (Exception e) {
            logger.error("Failed to persist activity log for endpoint {}: {}", activityLog.getEndpoint(), e.getMessage());
        }
    }
}

package com.caliper.activity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.activity.entity.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByUserId(String userId);

    List<ActivityLog> findByUserIdAndTimestampBetween(String userId, Date from, Date to);

    List<ActivityLog> findByClientId(String clientId);
}

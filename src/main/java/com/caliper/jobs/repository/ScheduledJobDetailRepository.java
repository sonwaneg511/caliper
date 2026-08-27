package com.caliper.jobs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.caliper.jobs.entity.ScheduledJobDetail;

public interface ScheduledJobDetailRepository extends JpaRepository<ScheduledJobDetail, Long> {

}

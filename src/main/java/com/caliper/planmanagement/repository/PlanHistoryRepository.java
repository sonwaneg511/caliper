package com.caliper.planmanagement.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.planmanagement.entity.PlanHistory;

public interface PlanHistoryRepository extends JpaRepository<PlanHistory, Long> {
	List<PlanHistory> findByClientIdOrderByCreatedAtDesc(String clientId);
	Page<PlanHistory> findByClientIdOrderByCreatedAtDesc(String clientId, Pageable pageable);
}

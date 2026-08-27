package com.caliper.planmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.caliper.planmanagement.entity.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long>{
	@Query("SELECT MAX(p.id) FROM Plan p")
    Long findLatestId();
	
	Plan findTopByOrderByIdDesc();
	
	Plan findByClientId(String clientId);
}

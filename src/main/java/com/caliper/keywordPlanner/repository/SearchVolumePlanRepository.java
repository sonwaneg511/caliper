package com.caliper.keywordPlanner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.keywordPlanner.entity.SearchVolumePlan;

@Repository
public interface SearchVolumePlanRepository extends JpaRepository<SearchVolumePlan, Long>{

	List<SearchVolumePlan> findAllByIsReportReadyAndPlanType(boolean isReportReady, String planType);

}

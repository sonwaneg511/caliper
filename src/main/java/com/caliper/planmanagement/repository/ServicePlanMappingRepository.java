package com.caliper.planmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.planmanagement.entity.ServicePlanMapping;

public interface ServicePlanMappingRepository extends JpaRepository<ServicePlanMapping, Long>{
	public List<ServicePlanMapping> findByPlanId(long planId);
}

package com.caliper.planmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.planmanagement.entity.PlanServiceMaster;

public interface PlanServiceMasterRepository extends JpaRepository<PlanServiceMaster, Long> {

    List<PlanServiceMaster> findAllByServiceKeyIn(List<String> serviceKeys);
}

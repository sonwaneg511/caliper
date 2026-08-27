package com.caliper.keywordPlanner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.keywordPlanner.entity.GoogleExpansion;

@Repository
public interface GoogleExpansionRepository extends JpaRepository<GoogleExpansion, Long>{

	List<GoogleExpansion> findAllByPlanID(long planID);
}

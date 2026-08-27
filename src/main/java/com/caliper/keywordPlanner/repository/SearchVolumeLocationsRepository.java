package com.caliper.keywordPlanner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.keywordPlanner.entity.SearchVolumeLocations;

@Repository
public interface SearchVolumeLocationsRepository extends JpaRepository<SearchVolumeLocations, Long>{

	List<SearchVolumeLocations> findAllByPlanID(Long planID);

	void deleteAllByPlanID(Long planID);
}

package com.caliper.keywordPlanner.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.keywordPlanner.entity.SearchVolumeData;

@Repository
public interface SearchVolumeDataRepository extends JpaRepository<SearchVolumeData, Long>{

	List<SearchVolumeData> findAllByPlanIDAndFetchSuccess(Long planID, boolean fetchSuccess);

	List<SearchVolumeData> findAllByPlanID(Long planID);

	Optional<SearchVolumeData> findByPlanIDAndKeywordAndLocation(Long planID, String keyword, String location);

	void deleteAllByPlanID(Long planID);
}

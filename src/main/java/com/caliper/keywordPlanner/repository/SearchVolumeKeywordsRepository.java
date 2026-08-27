package com.caliper.keywordPlanner.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.keywordPlanner.entity.SearchVolumeKeywords;

@Repository
public interface SearchVolumeKeywordsRepository extends JpaRepository<SearchVolumeKeywords, Long>{

	List<SearchVolumeKeywords> findAllByPlanID(Long planID);

	List<SearchVolumeKeywords> findAllByFetchSuccessAndPlanID(boolean fetchSuccess, Long planID);

	Optional<SearchVolumeKeywords> findByPlanIDAndKeyword(Long planID, String keyword);

	void deleteAllByPlanID(Long planID);
}

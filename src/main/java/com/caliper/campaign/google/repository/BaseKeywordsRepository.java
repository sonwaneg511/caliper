package com.caliper.campaign.google.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.campaign.google.entity.BaseKeywords;

public interface BaseKeywordsRepository extends JpaRepository<BaseKeywords, Long>{

	public Optional<BaseKeywords> getByKeyword(String keyword);
	
	public List<BaseKeywords> getBySourceValue(String sourceValue);
}

package com.caliper.campaign.google.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.google.entity.GoogleKeyword;

@Repository
public interface GoogleKeywordRepository extends JpaRepository<GoogleKeyword, Long>{
	public List<GoogleKeyword> findByAdgroupId(long adgroupId);
}

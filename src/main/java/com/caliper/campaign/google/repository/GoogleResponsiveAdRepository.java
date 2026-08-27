package com.caliper.campaign.google.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.google.entity.GoogleResponsiveAd;

@Repository
public interface GoogleResponsiveAdRepository extends JpaRepository<GoogleResponsiveAd, Long>{
	
	@Modifying
    @Query("DELETE FROM GoogleResponsiveAd g WHERE g.adgroupId = :adgroupId AND g.type = :type")
	void deleteByAdgroupIdAndType(Long adgroupId, String type);
	
	List<GoogleResponsiveAd> findAllByAdgroupId(long adgroupId);
	
	GoogleResponsiveAd findByAdgroupId(long adgroupId);
	
}

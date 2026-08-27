package com.caliper.reporting.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.caliper.reporting.entity.GMBInsightSettings;

import jakarta.transaction.Transactional;

public interface GMBInsightSettingsRepository extends JpaRepository<GMBInsightSettings, Long>{

	public List<GMBInsightSettings> findByClientId (String clientId);
	
	@Modifying
	@Transactional
	@Query(value = "update gmb_insight_settings set last_inserted_date = :lastInsertedDate where client_id = :clientId and dealer_id in (:dealerIds)", nativeQuery = true)
	public int updateGMBInsightSettingsByDealerId(@Param("lastInsertedDate") Date lastInsertedDate, @Param("clientId") String clientId, @Param("dealerIds") Set<String> dealerIds);

}

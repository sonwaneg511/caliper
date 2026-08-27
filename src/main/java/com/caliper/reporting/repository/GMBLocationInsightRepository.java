package com.caliper.reporting.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.caliper.reporting.entity.GMBLocationInsight;

import jakarta.transaction.Transactional;

@Repository
public interface GMBLocationInsightRepository extends JpaRepository<GMBLocationInsight, Long>, JpaSpecificationExecutor<GMBLocationInsight>{
	
	@Query(value = "select * from gmb_location_insight where client_id = :clientId and report_date >= :startDate and  report_date <= :endDate", nativeQuery = true)
	public List<GMBLocationInsight> getGmbLocationInsightByReportDate(@Param("clientId") String clientId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

	@Modifying
	@Transactional
	@Query(value = "delete from gmb_location_insight where client_id = :clientId and report_date >= :startDate and report_date <= :endDate and dealer_id in (:dealerIds)", nativeQuery = true)
	public void deleteGmbLocationInisghtByReportDateAndDealerId(@Param("clientId") String clientId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("dealerIds") Set<String> dealerIds);
	
	List<GMBLocationInsight> findByClientIdAndDealerIdInAndReportDateBetween(String clientId, Set<String>dealerIds, Date fromDate, Date toDate);

}

package com.caliper.location.gmb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.caliper.location.gmb.entity.GMBOperationHours;

@Repository
public interface GMBOperationHoursRepository extends JpaRepository<GMBOperationHours, Long>{

	List<GMBOperationHours> findByClientId(String clientId);

	@Modifying
	@Query("""
			    UPDATE GMBOperationHours h
			    SET
			        h.clientId = :clientId,
			        h.mondayOpenTime = :mondayOpenTime,
			        h.mondayCloseTime = :mondayCloseTime,
			        h.tuesdayOpenTime = :tuesdayOpenTime,
			        h.tuesdayCloseTime = :tuesdayCloseTime,
			        h.wednesdayOpenTime = :wednesdayOpenTime,
			        h.wednesdayCloseTime = :wednesdayCloseTime,
			        h.thursdayOpenTime = :thursdayOpenTime,
			        h.thursdayCloseTime = :thursdayCloseTime,
			        h.fridayOpenTime = :fridayOpenTime,
			        h.fridayCloseTime = :fridayCloseTime,
			        h.saturdayOpenTime = :saturdayOpenTime,
			        h.saturdayCloseTime = :saturdayCloseTime,
			        h.sundayOpenTime = :sundayOpenTime,
			        h.sundayCloseTime = :sundayCloseTime
			    WHERE h.gmbLocationId = :gmbLocationId
			""")
	int updateGMBOperationHours(
			@Param("clientId") String clientId,
			@Param("mondayOpenTime") String mondayOpenTime,
			@Param("mondayCloseTime") String mondayCloseTime,
			@Param("tuesdayOpenTime") String tuesdayOpenTime,
			@Param("tuesdayCloseTime") String tuesdayCloseTime,
			@Param("wednesdayOpenTime") String wednesdayOpenTime,
			@Param("wednesdayCloseTime") String wednesdayCloseTime,
			@Param("thursdayOpenTime") String thursdayOpenTime,
			@Param("thursdayCloseTime") String thursdayCloseTime,
			@Param("fridayOpenTime") String fridayOpenTime,
			@Param("fridayCloseTime") String fridayCloseTime,
			@Param("saturdayOpenTime") String saturdayOpenTime,
			@Param("saturdayCloseTime") String saturdayCloseTime,
			@Param("sundayOpenTime") String sundayOpenTime,
			@Param("sundayCloseTime") String sundayCloseTime,
			@Param("gmbLocationId") String gmbLocationId
			);

	GMBOperationHours findByClientIdAndGmbLocationId(String clientId, String gmbLocationId);
}
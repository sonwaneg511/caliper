package com.caliper.campaign.google.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.google.entity.GoogleCampaign;

@Repository
public interface GoogleCampaignRepository extends JpaRepository<GoogleCampaign, Long>{
	
	GoogleCampaign findTopByOrderByIdDesc();
	
	List<GoogleCampaign> findByClientIdAndDealerIdInAndStartDateBetween(
	        String clientId,
	        Set<String> dealerIds,
	        Date fromDate,
	        Date toDate
	); 
	
	@Query(
			  value = "SELECT * FROM google_campaign " +
			          "WHERE client_id = :clientId " +
			          "AND dealer_id::text IN :dealerIds " +
			          "AND start_date BETWEEN :fromDate AND :toDate",
			  nativeQuery = true
			)
			List<GoogleCampaign> findByClientIdAndDealerIdInAndStartDateBetweenNative(
			        @Param("clientId") String clientId,
			        @Param("dealerIds") Set<String> dealerIds,
			        @Param("fromDate") Date fromDate,
			        @Param("toDate") Date toDate
			);

	
	List<GoogleCampaign> findByClientIdAndDealerIdIn(String clientId, List<String> dealerIds);

	Page<GoogleCampaign> findByClientIdAndDealerIdInOrderByIdDesc(
	        String clientId,
	        List<String> dealerIds,
	        Pageable pageable
	);

	Page<GoogleCampaign> findByClientIdAndDealerIdInAndCampaignNameContainingIgnoreCaseOrderByIdDesc(
	        String clientId,
	        List<String> dealerIds,
	        String campaignName,
	        Pageable pageable
	);

	Page<GoogleCampaign> findByStatusOrderByIdDesc(
	        String platform,
	        Pageable pageable
	);

	Page<GoogleCampaign> findByStatusAndCampaignNameContainingIgnoreCaseOrderByIdDesc(
	        String platform,
	        String campaignName,
	        Pageable pageable
	);
	
	public List<GoogleCampaign> findAllGoogleCampaignByStatus(String status);
	public boolean existsByCampaignNameAndClientId(String campaignName, String clientId);
	public boolean findByCampaignNameAndClientId(String campaignName, String clientId);
}

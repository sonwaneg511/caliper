package com.caliper.location.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.caliper.location.entity.DealerLocation;

@Repository
public interface DealerLocationRepository extends JpaRepository<DealerLocation, Long>, JpaSpecificationExecutor<DealerLocation>{

	
	Page<DealerLocation> findByClientIdAndDealerNameContainingIgnoreCase(String clientId, String dealerName, Pageable pageable);
	
/*	Page<DealerLocation> findByClientIdAndDealerIdInAndDealerNameContainingIgnoreCase(
	        String clientId,
	        Set<String> dealerIds,
	        String dealerName,
	        Pageable pageable
	);
*/
	@Query("""
		    SELECT d
		    FROM DealerLocation d
		    WHERE d.clientId = :clientId
		      AND d.dealerId IN :dealerIds
		      AND (
		            LOWER(d.dealerName) LIKE LOWER(CONCAT('%', :searchText, '%'))
		         OR LOWER(d.dealerId) LIKE LOWER(CONCAT('%', :searchText, '%'))
		         OR LOWER(d.city) LIKE LOWER(CONCAT('%', :searchText, '%'))
		         OR LOWER(d.state) LIKE LOWER(CONCAT('%', :searchText, '%'))
		         OR LOWER(d.area) LIKE LOWER(CONCAT('%', :searchText, '%'))
		      )
		""")
		Page<DealerLocation> searchLocations(
		        @Param("clientId") String clientId,
		        @Param("dealerIds") Set<String> dealerIds,
		        @Param("searchText") String searchText,
		        Pageable pageable);
	
	Page<DealerLocation> findByClientIdAndDealerIdIn(String clientId, Pageable pageable, Set<String>mappedDealers);

	long countByClientIdAndDealerIdIn(String clientId, Set<String> dealerIds);
	
	@Query(value = "select * from dealer_location where dealer_id = :dealerId and client_id = :clientId", nativeQuery = true)
	public DealerLocation getDealerLocationByDealerIdAndClientId(@Param(value = "dealerId") String dealerId, @Param("clientId") String clientId);
	

	List<DealerLocation> findByClientId(String clientId);

	List<DealerLocation> findByDealerIdIn(Set<String> dealerIds);
	
	List<DealerLocation> findByDealerIdInAndClientId(Set<String> dealerIds, String clientid);


	@Modifying
	@Query(value = "UPDATE gmb_location " +"SET is_qr_created = :qrCreated, qr_image = :qrImage " +"WHERE dealer_id = :dealerId and client_id = :clientId", nativeQuery = true)
	void updateQrInfoByDealerId(
	        @Param("qrCreated") boolean qrCreated,
	        @Param("qrImage") String qrImage,
	        @Param("dealerId") String dealerId,
	        @Param("clientId") String clientId);

	  Optional<DealerLocation> findByClientIdAndDealerId(String clientId, String dealerId);

}

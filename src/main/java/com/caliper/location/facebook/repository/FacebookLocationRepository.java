package com.caliper.location.facebook.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.caliper.location.facebook.entity.FacebookLocation;

@Repository
public interface FacebookLocationRepository extends JpaRepository<FacebookLocation, String>{

	public FacebookLocation getFacebookLocationByClientIdAndFacebookPageId(@Param("clientId") String clientId,@Param("facebookPageId") String facebookPageId);
	
	@Query(name = "select * from facebook_location where client_id = :clientId and dealer_id = :dealerId",nativeQuery = true)
	public FacebookLocation getFacebookLocationByClientIdAndDealerId(@Param("clientId") String clientId, @Param("dealerId") String dealerId);

	public List<FacebookLocation> findByClientId(String clientId);
	
	public FacebookLocation findByClientIdAndDealerId(String clientId, String dealerId);
	
	public List<FacebookLocation> findByClientIdAndDealerIdIn(String clientId, List<String> dealerId);
	
    long countByClientId(String clientId);
    
	long countByClientIdAndDealerIdIn(String clientId, Set<String> dealerIds);

}

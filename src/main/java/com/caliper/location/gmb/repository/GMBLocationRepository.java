package com.caliper.location.gmb.repository;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.location.gmb.entity.GMBLocation;

@Repository
public interface GMBLocationRepository extends JpaRepository<GMBLocation,String>, JpaSpecificationExecutor<GMBLocation> {

	List<GMBLocation> findByClientId(String clientId);
	
	List<GMBLocation> findByAccountId(String accountId);
	
	List<GMBLocation> findByAccountIdAndClientId(String accountId, String clientId);

	long countByClientIdAndDealerIdIn(String clientId, Set<String> dealerIds);

    
	GMBLocation findByClientIdAndGmbLocationId(String clientId, String gmbLocationId);

	@Query(value = "select * from gmb_location where client_id = :clientId and dealer_id = :dealerId", nativeQuery = true)
	public GMBLocation getAllGmbLocationByClientIdAndDealerId(@Param(value = "clientId") String clientId, @Param(value = "dealerId") String dealerId);
	
	public List<GMBLocation> findByClientIdAndDealerIdIn(String clientId, List<String> dealerId);

	@Query(value = "select * from gmb_location where client_id = :clientId", nativeQuery = true)
	public List<GMBLocation> getAllGmbLocationByClientId(@Param(value = "clientId") String clientId);

	@Modifying
	@Query(value = "UPDATE gmb_location SET place_action_id = :placeActionId, appointment_link = :appointmentLink " +
			"WHERE dealer_id = :dealerId",nativeQuery = true)
	public void updatePlaceActionAndAppointmentLink(@Param("placeActionId") String placeActionId,@Param("appointmentLink") String appointmentLink,
			@Param("dealerId") String dealerId);

	@Modifying
	@Transactional
	@Query(value = "UPDATE gmb_location " +
			"SET whatsapp_url = :whatsappUrl, " +
			"instagram_url = :instagramUrl, " +
			"facebook_url = :facebookUrl, " +
			"twitter_url = :twitterUrl, " +
			"youtube_url = :youtubeUrl, " +
			"linkedin_url = :linkedinUrl " +
			"WHERE dealer_id = :dealerId", nativeQuery = true)
	public void updateAttributesByDealerId(
			@Param("whatsappUrl") String whatsappUrl,
			@Param("instagramUrl") String instagramUrl,
			@Param("facebookUrl") String facebookUrl,
			@Param("twitterUrl") String twitterUrl,
			@Param("youtubeUrl") String youtubeUrl,
			@Param("linkedinUrl") String linkedinUrl,
			@Param("dealerId") String dealerId);


	@Modifying
	@Transactional
	@Query("UPDATE GMBLocation SET status = :status WHERE clientId = :clientId AND gmbLocationId = :gmbLocationId")
	public void updateStatusByClientIdAndGmbLocationId(
			@Param("status") String status,
			@Param("clientId") String clientId,
			@Param("gmbLocationId") String gmbLocationId
			);

}

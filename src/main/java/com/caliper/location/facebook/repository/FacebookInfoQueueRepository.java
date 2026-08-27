package com.caliper.location.facebook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.caliper.location.facebook.entity.FacebookInfoQueue;
@Repository
public interface FacebookInfoQueueRepository extends JpaRepository<FacebookInfoQueue, Long>{

	public List<FacebookInfoQueue> findAllFacebookInfoQueueByClientId(String clientId);
	
	@Modifying
	@Query(value = "UPDATE facebook_info_queue SET status = :status WHERE id = :id", nativeQuery = true)
	void updateFacebookInfoQueueStatusById(@Param("status") String status, @Param("id") Long id);

	public List<FacebookInfoQueue> findByStatus(String statusSubmit);
}

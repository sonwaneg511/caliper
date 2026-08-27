package com.caliper.location.gmb.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.caliper.location.gmb.entity.GMBInfoQueue;

@Repository
public interface GMBInfoQueueRepository extends JpaRepository<GMBInfoQueue, Long>{

	@Query(value = "select * from gmb_info_queue where client_id = :clientId", nativeQuery = true)
	public List<GMBInfoQueue> getAllGMBInfoQueueByClientId(@Param(value = "clientId") String clientId);

	@Modifying
	@Query(value = "UPDATE gmb_info_queue SET status = :status WHERE id = :id", nativeQuery = true)
	void updateGMBInfoQueueStatusById(@Param("status") String status, @Param("id") Long id);

	public List<GMBInfoQueue> findByStatus(String status);
}

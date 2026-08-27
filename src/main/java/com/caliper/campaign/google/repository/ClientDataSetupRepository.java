package com.caliper.campaign.google.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.caliper.campaign.google.entity.ClientDataSetup;

public interface ClientDataSetupRepository extends JpaRepository<ClientDataSetup, Long>{
	
	@Query(name = "select * from client_data_setup  where client_id = :clientId", nativeQuery = true)
	public ClientDataSetup findClientDataSetupByClientId(@Param("clientId") String clientId);

}

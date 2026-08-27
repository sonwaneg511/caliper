package com.caliper.keywordPlanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.keywordPlanner.entity.GoogleAccount;

@Repository
public interface GoogleAccountRepository extends JpaRepository<GoogleAccount, Long>{

	GoogleAccount findByClientId(String clientId);
	
	GoogleAccount findByAccountId(String accountId);
}

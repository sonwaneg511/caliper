package com.caliper.location.gmb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.location.gmb.entity.GMBAccount;

@Repository
public interface GMBAccountRepository  extends JpaRepository<GMBAccount, Long> {

	List<GMBAccount> findByClientId(String clientId);
	
	List<GMBAccount> findByClientIdAndStatus(String clientId, String status);
	
	Optional<GMBAccount> findByClientIdAndAccountName(String clientId, String accountName);

	GMBAccount findTopByClientIdOrderByLastModifiedDateDesc(String clientId);
	
	@Transactional
    @Modifying
    @Query("UPDATE GMBAccount g SET g.status = CASE WHEN g.accountId IN :accountIds THEN 'selected' ELSE 'Not selected' END WHERE g.clientId = :clientId")
    int updateSelectedAccountStatus(
            @Param("clientId") String clientId,
            @Param("accountIds") List<String> accountIds);
}

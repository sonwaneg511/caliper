package com.caliper.location.facebook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.location.facebook.entity.FacebookAccount;

@Repository
public interface FacebookAccountRepository extends JpaRepository<FacebookAccount, Long> {

    Optional<FacebookAccount> findByClientId(String clientId);

    Optional<FacebookAccount> findByClientIdAndDealerId(String clientId, String dealerId);

    Optional<FacebookAccount> findByClientIdAndDealerIdIsNull(String clientId);
}

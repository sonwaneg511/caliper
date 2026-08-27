package com.caliper.location.gmb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.location.gmb.entity.GMBOAuthToken;

@Repository
public interface GMBOAuthTokenRepository extends JpaRepository<GMBOAuthToken, Long> {

    GMBOAuthToken findByClientId(String clientId);

    GMBOAuthToken findFirstByOrderByIdAsc();
}

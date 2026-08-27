package com.caliper.location.facebook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.location.facebook.entity.FacebookPage;

@Repository
public interface FacebookPageRepository extends JpaRepository<FacebookPage, Long>{

	List<FacebookPage> findByClientId(String clientId);

	Optional<FacebookPage> findByClientIdAndDealerId(String clientId, String dealerId);

	Optional<FacebookPage> findByClientIdAndDealerIdIsNull(String clientId);

}

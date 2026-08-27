package com.caliper.campaign.google.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.google.entity.GoogleCallAd;

@Repository
public interface GoogleCallAdRepository extends JpaRepository<GoogleCallAd, Long>{

	Optional<GoogleCallAd> findByAdGroupId(long adGroupId);

}

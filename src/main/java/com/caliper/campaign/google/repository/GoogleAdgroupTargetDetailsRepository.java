package com.caliper.campaign.google.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.campaign.google.entity.GoogleAdgroupTargetDetails;

@Repository
public interface GoogleAdgroupTargetDetailsRepository extends JpaRepository<GoogleAdgroupTargetDetails, Long>{

}

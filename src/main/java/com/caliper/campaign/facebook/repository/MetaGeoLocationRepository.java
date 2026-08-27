package com.caliper.campaign.facebook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.campaign.facebook.entity.MetaGeoLocation;

public interface MetaGeoLocationRepository extends JpaRepository<MetaGeoLocation, Long> {

    List<MetaGeoLocation> findByLocationTypeOrderByNameAsc(String locationType);

    List<MetaGeoLocation> findByNameContainingIgnoreCaseAndLocationTypeOrderByNameAsc(
            String name, String locationType);

    Optional<MetaGeoLocation> findFirstByNameIgnoreCaseAndLocationType(
            String name, String locationType);

    Optional<MetaGeoLocation> findFirstByMetaKeyAndLocationType(
            String metaKey, String locationType);

    boolean existsByMetaKeyAndLocationType(String metaKey, String locationType);
}

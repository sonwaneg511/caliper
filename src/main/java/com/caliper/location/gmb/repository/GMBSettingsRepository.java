package com.caliper.location.gmb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.location.gmb.entity.GMBSettings;

public interface GMBSettingsRepository extends JpaRepository<GMBSettings, String> {

	GMBSettings getByName(String name);
}

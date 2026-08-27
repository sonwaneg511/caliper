package com.caliper.location.config.repository;

import com.caliper.location.config.entity.OAuthAppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthAppConfigRepository extends JpaRepository<OAuthAppConfig, String> {
}

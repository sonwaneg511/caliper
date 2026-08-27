package com.caliper.utils.gemini.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.utils.gemini.entity.AIAdCopy;


public interface AIAdCopyRepository extends JpaRepository<AIAdCopy, Long>{

	List<AIAdCopy> findByClientId(String clientId);

}

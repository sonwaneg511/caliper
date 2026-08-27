package com.caliper.utils.gemini.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.caliper.utils.gemini.entity.GeminiAPICred;


public interface GeminiAPICredRepository extends JpaRepository<GeminiAPICred, Long>{
	 @Query("SELECT g FROM GeminiAPICred g")
	    GeminiAPICred getGeminiAICred();
}

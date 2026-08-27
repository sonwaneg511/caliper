package com.caliper.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class CorsConfig {

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowedOrigins(List.of(
	        "https://thecaliper.ai",
	        "https://uat.thecaliper.ai",
	        "https://3.6.168.97",
            "https://frontend-uat.thecaliper.ai",
            "https://dev.thecaliper.ai",
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:3002",
            "http://localhost:3003"
	    ));
	    
	   // config.setAllowedOriginPatterns(List.of("*"));
	    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
	    config.setAllowedHeaders(List.of("*"));
	    config.setAllowCredentials(true);
	    config.setExposedHeaders(List.of("Set-Cookie"));


	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource() {
	        @Override
	        public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
	            CorsConfiguration cors = super.getCorsConfiguration(request);
	            if (cors != null && request.getHeader("Origin") != null) {
	                cors.setAllowedOrigins(List.of(request.getHeader("Origin")));
	            }
	            return cors;
	        }
	    };

	    source.registerCorsConfiguration("/**", config);
	    return source;
	}

}


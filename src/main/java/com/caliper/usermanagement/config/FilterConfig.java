package com.caliper.usermanagement.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.caliper.usermanagement.authfilter.AuthorizationFilter;

@Configuration
public class FilterConfig {

//	private final AuthorizationFilter authorizationFilter;
//
//	@Autowired
//	public FilterConfig(AuthorizationFilter authorizationFilter) {
//		this.authorizationFilter = authorizationFilter;
//	}

//	@Bean
//	public FilterRegistrationBean<AuthorizationFilter> authorizationFilterRegistration(){//(AuthorizationFilter filter) {
//		FilterRegistrationBean<AuthorizationFilter> registrationBean = new FilterRegistrationBean<>();
//		// registrationBean.setFilter(filter);
//		registrationBean.setFilter(authorizationFilter);
//		registrationBean.addUrlPatterns("/post/*", "/review/*", "/location/*", "/campaign/*");
//		registrationBean.setOrder(1);
//		return registrationBean;
//	}

//	@Bean
//	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//		 http
//	        .csrf(csrf -> csrf.disable())
//	        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//	        .authorizeHttpRequests(auth -> auth
//	            .requestMatchers("/post/**", "/review/**", "/location/**", "/campaign/**").authenticated()
//	            .anyRequest().permitAll()
//	        )
//	        .addFilterBefore(
//	            authorizationFilter,
//	            UsernamePasswordAuthenticationFilter.class
//	        );
//		return http.build();
//	}
}





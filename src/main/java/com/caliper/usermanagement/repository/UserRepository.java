package com.caliper.usermanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.usermanagement.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
	Optional<User> findByUserId(String userId);
	
	List<User> findAllByUserId(String userId);

	List<User> findAllByClientId(String clientId);
	
	Optional<User> findByUserName(String userName);
	
	User findTopByOrderByIdDesc();

	boolean existsByUserId(String userId);
	
	List<User> findByUserIdIn(List<String> userIds);
	
	Optional<User> findByUserIdAndClientId(String userId, String clientId);

}

package com.caliper.usermanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.usermanagement.entity.RoleMaster;


public interface RoleMasterRepository extends JpaRepository<RoleMaster, Long>{
	List<RoleMaster> findAllRoleMasterById(long id);
}

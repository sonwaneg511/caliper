package com.caliper.usermanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.entity.UserRoleClientMapping;

@Repository
public interface UserRoleClientMappingRepository extends JpaRepository<UserRoleClientMapping, Long>{

    @Query(value = "Select * from user_role_client_mapping where user_id = :userId", nativeQuery = true)
    public List<UserRoleClientMapping> getUserRoleClientMappingByUserId(@Param("userId") String userId);

    public List<UserRoleClientMapping> findByUserIdAndClientId(String userId, String clientId);
    
    //public UserRoleClientMapping findByUserIdAndClientId(String userId, String clientId);
    
    @Modifying
    @Transactional
    @Query(
      value = "delete from user_role_client_mapping where user_id = :userId and client_id = :clientId",
      nativeQuery = true
    )
    void deleteByUserIdAndClientId(
            @Param("userId") String userId,
            @Param("clientId") String clientId);
    
    public List<UserRoleClientMapping> getUserRoleClientMappingByUserIdAndClientId(String userId, String clientId);
}
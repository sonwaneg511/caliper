package com.caliper.usermanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.entity.UserRoleClientMapping;

import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserClientLocMappingRepository extends JpaRepository<UserClientLocMapping, Long>{

    boolean existsByUserIdAndDealerId(String userId, String dealerId);

    List<UserClientLocMapping> findByUserId(@Param("userId") String userId);
    
    @Query(value = "select * from user_client_loc_mapping where user_id = :userId and client_id = :clientId", nativeQuery = true)
    List<UserClientLocMapping> findByUserIdAndclientId(@Param("userId") String userId, @Param("clientId") String clientId);

    @Query(value = "Select * from user_client_loc_mapping where user_id = :userId", nativeQuery = true)
    public List<UserClientLocMapping> getUserClientLocMappingByUserId(@Param("userId") String userId);
    
    @Modifying
    @Transactional
    @Query(
      value = "delete from user_client_loc_mapping where user_id = :userId and client_id = :clientId",
      nativeQuery = true
    )
    void deleteByUserIdAndClientId(
            @Param("userId") String userId,
            @Param("clientId") String clientId);

}

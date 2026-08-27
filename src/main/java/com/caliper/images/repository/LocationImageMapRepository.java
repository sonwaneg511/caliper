package com.caliper.images.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.images.entity.LocationImageMap;
import com.caliper.images.entity.LocationImageMapId;

@Repository
public interface LocationImageMapRepository extends JpaRepository<LocationImageMap, LocationImageMapId> {

    List<LocationImageMap> findByImageId(Long imageId);

    List<LocationImageMap> findAllByImageIdIn(Collection<Long> imageIds);

    List<LocationImageMap> findAllByStatus(String status);

    List<LocationImageMap> findAllByClientIdAndStatus(String clientId, String status);

    boolean existsByDealerIdAndConsoleImageId(String dealerId, String consoleImageId);

    @Query("SELECT m FROM LocationImageMap m WHERE m.dealerId = :dealerId AND m.status = :status AND m.locationImage.imageCategory = :category")
    Optional<LocationImageMap> findByDealerIdAndStatusAndImageCategory(@Param("dealerId") String dealerId,
            @Param("status") String status, @Param("category") String category);

    @Query("SELECT m FROM LocationImageMap m JOIN FETCH m.locationImage WHERE m.dealerId = :dealerId AND m.status <> :excludedStatus")
    List<LocationImageMap> findAllByDealerIdAndStatusNotWithImage(@Param("dealerId") String dealerId,
            @Param("excludedStatus") String excludedStatus);

    @Query("SELECT m FROM LocationImageMap m WHERE m.dealerId = :dealerId AND m.locationImage.imageCategory = :category")
    List<LocationImageMap> findAllByDealerIdAndImageCategory(@Param("dealerId") String dealerId,
            @Param("category") String category);

    @Query("SELECT m FROM LocationImageMap m WHERE m.dealerId = :dealerId AND m.locationImage.imageCategory = :category AND m.locationImage.platform = :platform")
    List<LocationImageMap> findAllByDealerIdAndImageCategoryAndPlatform(@Param("dealerId") String dealerId,
            @Param("category") String category, @Param("platform") String platform);

    @Query("SELECT m FROM LocationImageMap m WHERE m.dealerId = :dealerId AND m.status = :status AND m.locationImage.imageCategory = :category AND m.locationImage.platform = :platform")
    Optional<LocationImageMap> findByDealerIdAndStatusAndImageCategoryAndPlatform(@Param("dealerId") String dealerId,
            @Param("status") String status, @Param("category") String category, @Param("platform") String platform);

    @Query("SELECT m FROM LocationImageMap m WHERE m.status = :status AND m.locationImage.platform = :platform")
    List<LocationImageMap> findAllByStatusAndImagePlatform(@Param("status") String status, @Param("platform") String platform);

    @Query("SELECT m FROM LocationImageMap m JOIN FETCH m.locationImage li WHERE m.dealerId = :dealerId AND m.status = :status AND li.platform = :platform")
    List<LocationImageMap> findAllByDealerIdAndStatusAndImagePlatform(@Param("dealerId") String dealerId,
            @Param("status") String status, @Param("platform") String platform);

    @Transactional
    @Modifying
    @Query("UPDATE LocationImageMap m SET m.consoleImageId = :consoleImageId, m.status = :status " +
           "WHERE m.dealerId = :dealerId AND m.imageId = :imageId")
    void updateConsoleImageIdAndStatusByImageIdAndDealerId(
            @Param("consoleImageId") String consoleImageId,
            @Param("status") String status,
            @Param("dealerId") String dealerId,
            @Param("imageId") Long imageId);

    @Transactional
    @Modifying
    @Query("UPDATE LocationImageMap m SET m.status = :setStatus WHERE m.status = :currentStatus")
    void updateAllByStatus(@Param("setStatus") String setStatus, @Param("currentStatus") String currentStatus);

    @Transactional
    @Modifying
    @Query("UPDATE LocationImageMap m SET m.status = :setStatus WHERE m.status = :currentStatus AND m.locationImage.platform = :platform")
    void updateAllByStatusAndImagePlatform(@Param("setStatus") String setStatus,
                                           @Param("currentStatus") String currentStatus,
                                           @Param("platform") String platform);
}

package com.caliper.images.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.caliper.images.entity.LocationImage;

@Repository
public interface LocationImageRepository extends JpaRepository<LocationImage, Long> {

    List<LocationImage> findAllByStatus(String status);

    List<LocationImage> findAllByImageIdIn(Collection<Long> imageIds);

    LocationImage findByClientIdAndImageId(String clientId, Long imageId);

    Page<LocationImage> findAllByClientId(String clientId, Pageable pageable);

    Page<LocationImage> findAllByClientIdAndImageCategory(String clientId, String imageCategory, Pageable pageable);

    Page<LocationImage> findAllByClientIdAndStatus(String clientId, String status, Pageable pageable);

    Page<LocationImage> findAllByClientIdAndImageCategoryAndStatus(String clientId, String imageCategory, String status, Pageable pageable);

    @Modifying
    @Query("UPDATE LocationImage i SET i.status = :status, i.comment = :comment WHERE i.imageId = :imageId")
    void updateStatusAndCommentById(@Param("status") String status, @Param("comment") String comment, @Param("imageId") Long imageId);
}

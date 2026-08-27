package com.caliper.location.gmb.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.caliper.location.gmb.entity.GMBCategory;

@Repository
public interface GMBCategoryRepository extends JpaRepository<GMBCategory, Long>{

	@Query(value = "select * from gmb_category where category_id = :categoryId", nativeQuery = true)
	public GMBCategory getGMBCategoryByCategoryId(@Param(value = "categoryId") String categoryId);
	
}

package com.caliper.location.facebook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caliper.location.facebook.entity.FacebookCategory;

@Repository
public interface FacebookCategoryRepository extends JpaRepository<FacebookCategory, Long>{

}

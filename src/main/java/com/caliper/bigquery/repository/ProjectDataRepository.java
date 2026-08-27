package com.caliper.bigquery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.bigquery.entity.ProjectData;

public interface ProjectDataRepository extends JpaRepository<ProjectData, Long>{

}

package com.caliper.post.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.caliper.post.entity.PostLocationMap;
import com.caliper.post.entity.PostLocationMapId;

import jakarta.transaction.Transactional;

public interface PostLocationMapRepository extends JpaRepository<PostLocationMap, PostLocationMapId> , JpaSpecificationExecutor<PostLocationMap>{

	@Query(value = "Select * from post_location_map where post_id = :postId and platform = :platform", nativeQuery = true)
	public List<PostLocationMap> getPostLocationMapByPostIdAndPlatform (@Param("postId") long postId, @Param("platform") String platform);

	@Query(value = "select * from post_location_map where status = :status and platform = :platform order by post_id asc", nativeQuery = true)
	public List<PostLocationMap> getAllPostLocationMapByStatus (@Param("status") String status, @Param("platform") String platform);

	@Query(value = "select * from post_location_map where client_id = :clientId and  platform = :platform", nativeQuery = true)
	public List<PostLocationMap> getAllPostLocationMapByClientIdAndPlatform (@Param("clientId") String clientId, @Param("platform") String platform);

	@Query(value = " SELECT * FROM post_location_map WHERE post_id IN (:postIds) AND platform = :platform ", nativeQuery = true)
	public List<PostLocationMap> getPostLocationMapByPostIdsAndPlatform(@Param("postIds") List<Long> postIds, @Param("platform") String platform);
	
	@Query(value = " SELECT * FROM post_location_map WHERE post_id IN (:postIds)", nativeQuery = true)
	public List<PostLocationMap> getPostLocationMapByPostIds(@Param("postIds") List<Long> postIds);
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "update  post_location_map  set console_post_id = :consolePostId, status = :status where dealer_id = :dealerId and post_id = :postId", nativeQuery = true)
	public void updatePostLocationMapconsolePostIdAndStatusByPostId (@Param("consolePostId") String consolePostId, @Param("status") String status, @Param("dealerId") String dealerId, @Param("postId") long postId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "update  post_location_map  set status = :setStatus where status = :status", nativeQuery = true)
	public void updatePostLocationMapStatus (@Param("setStatus") String setStatus, @Param("status") String status);

	
	public List<PostLocationMap> findByClientIdAndDealerIdInAndCreatedDateBetween(String clientId, Set<String> mappedDealers, Date fromDate, Date toDate);

}

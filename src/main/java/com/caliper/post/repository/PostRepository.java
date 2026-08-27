package com.caliper.post.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.caliper.post.entity.Post;
import com.caliper.post.entity.PostLocationMap;

import jakarta.transaction.Transactional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post>{

	@Query(value = "SELECT * FROM post WHERE status = :status AND platform = :platform AND created_date >= :startDate AND created_date <= :endDate ORDER BY id DESC", nativeQuery = true)
	public List<Post> getAllPostByStatus(@Param("status") String status, @Param("platform") String platform, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

	@Modifying
	@Transactional
	@Query(value = "update post set status = :status, comment = :comment where id = :id", nativeQuery = true)
	public void updateStatusAndCommentById(@Param("status") String status, @Param("comment") String comment, @Param("id") long id);

	@Query(value = "SELECT * FROM post WHERE client_id = :clientId AND platform = :platform", nativeQuery = true)
	public List<Post> getAllPostByClientIdAndSource(@Param("clientId") String clientId, @Param("platform") String source);
	
	@Query(value = "SELECT * FROM post WHERE client_id = :clientId AND platform = :platform AND created_date >= :startDate and created_date <= :endDate", nativeQuery = true)
	public List<Post> getAllPostByClientIdAndSourceAndCreatedDate(@Param("clientId") String clientId, @Param("platform") String source, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
	
	
	@Query(value = "SELECT * FROM post WHERE client_id = :clientId AND post_id = :postId AND platform = :platform", nativeQuery = true)
	public Post getPostByclientIdAndPostIdAndPlatform(String clientId, long postId, String platform);
	
	@Query(value = "SELECT * FROM post WHERE client_id = :clientId and post_id IN (:postIds)", nativeQuery = true)
	public List<Post> getPostByPostIdsAndPlatform(@Param("clientId") String clientId, @Param("postIds") Set<Long> postIds);

}

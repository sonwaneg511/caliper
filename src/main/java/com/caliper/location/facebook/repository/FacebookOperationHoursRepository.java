package com.caliper.location.facebook.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.caliper.location.facebook.entity.FacebookOperationHours;

@Repository
public interface FacebookOperationHoursRepository extends JpaRepository<FacebookOperationHours, Long>{

	public List<FacebookOperationHours> findByClientId(String clientId);
	
	Optional<FacebookOperationHours> findByClientIdAndFbPageId(String clientId, String fbPageId);
}

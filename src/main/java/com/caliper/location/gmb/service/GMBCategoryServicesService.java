package com.caliper.location.gmb.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.caliper.location.gmb.entity.GMBCategoryServices;
import com.caliper.location.gmb.repository.GMBCategoryServicesRepository;

@Service
public class GMBCategoryServicesService {

	@Autowired
	private GMBCategoryServicesRepository categoryServicesRepository;
	
	public List<GMBCategoryServices> getAllGMBCategoryServices(){
		return categoryServicesRepository.findAll();
	}
}

package com.caliper.location.gmb.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.gmb.entity.GMBCategory;
import com.caliper.location.gmb.repository.GMBCategoryRepository;

@Service
public class GMBCategoryService {

	@Autowired
	private GMBCategoryRepository categoryRepository;
	
	public List<GMBCategory> getAllGMBCategory(){
		return categoryRepository.findAll();
	}
	
	public GMBCategory getGMBCategoryByCategoryId(String categoryId) {
		
		return categoryRepository.getGMBCategoryByCategoryId(categoryId);
	}
}

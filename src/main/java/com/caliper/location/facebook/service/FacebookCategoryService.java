package com.caliper.location.facebook.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.facebook.entity.FacebookCategory;
import com.caliper.location.facebook.repository.FacebookCategoryRepository;
import com.caliper.utils.exception.customException.ResourceNotFoundException;

@Service
public class FacebookCategoryService {

	@Autowired
	private FacebookCategoryRepository facebookCategoryRepository;
	
	public List<FacebookCategory> getAllFbCategory(){
		return facebookCategoryRepository.findAll();
	}
	
	public FacebookCategory getFacebookCategoryById(Long id) {
		return facebookCategoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Facebook category not found: " + id));
	}
}

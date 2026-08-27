package com.caliper.location.facebook.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.facebook.entity.FacebookPage;
import com.caliper.location.facebook.repository.FacebookPageRepository;

@Service
public class FacebookPageService {

	@Autowired
	private FacebookPageRepository facebookPageRepository;
	
	public List<FacebookPage> getFacebookAccountByClientId(String clientId) {
		return facebookPageRepository.findByClientId(clientId);
		
	}
	
	public  List<FacebookPage> getAllFacebookPages(){
		return facebookPageRepository.findAll();
	}
}

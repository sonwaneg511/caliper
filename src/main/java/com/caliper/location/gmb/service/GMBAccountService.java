package com.caliper.location.gmb.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.gmb.entity.GMBAccount;
import com.caliper.location.gmb.repository.GMBAccountRepository;
@Service
public class GMBAccountService {
	
	@Autowired
	private GMBAccountRepository gmbAccountRepository;
	
	
	public List<GMBAccount> findByClientId(String clientId){
		return gmbAccountRepository.findByClientId(clientId);
	}
	

}

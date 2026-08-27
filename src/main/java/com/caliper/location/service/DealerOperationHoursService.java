package com.caliper.location.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.repository.DealerOperationHoursRepository;

@Service
public class DealerOperationHoursService {

	@Autowired
	private DealerOperationHoursRepository dealerOperationHoursRepository;
}

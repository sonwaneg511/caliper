package com.caliper.location.gmb.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.location.gmb.service.GMBLocationService;

@RestController
public class GMBLocationController {

	@Autowired
	private GMBLocationService gmbLocationService;

	
}

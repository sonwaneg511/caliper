package com.caliper.location.facebook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.location.facebook.service.FacebookLocationService;

@RestController
public class FacebookLocationController {

	@Autowired
	private FacebookLocationService facebookLocationService;
}

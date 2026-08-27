package com.caliper.location.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.dto.response.FilteredDealerLocationResponse;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.service.DealerLocationService;

@RestController
@RequestMapping("locations")
public class DealerLocationController {

	@Autowired
	private DealerLocationService dealerLocationService;

	@GetMapping("/filtered-locations")
	public ResponseEntity<List<DealerLocation>> getFilteredLocationPageResponse(@RequestBody LocationFilterRequest req){
		return ResponseEntity.ok(dealerLocationService.getFilteredDealerLocation(req));
	}

	@PostMapping("/dropdown-filtered-locations")
	public ResponseEntity<FilteredDealerLocationResponse> getDropDownFilteredLocationPageResponse(@RequestBody LocationFilterRequest req){
		return ResponseEntity.ok(dealerLocationService.getDropDownFilteredDealerLocation(req));
	}
}
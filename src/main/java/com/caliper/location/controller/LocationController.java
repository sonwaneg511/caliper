package com.caliper.location.controller;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.location.dto.CampaignSettings;
import com.caliper.location.dto.LocationDetailsDto;
import com.caliper.location.dto.request.CampaignSettingsBody;
import com.caliper.location.dto.request.FacebookDetailsBody;
import com.caliper.location.dto.request.GMBDetailsBody;
import com.caliper.location.dto.request.InfoRequestDto;
import com.caliper.location.dto.request.LocationOverviewBody;
import com.caliper.location.dto.response.AccountVerifyResponseDto;
import com.caliper.location.dto.response.CategoryDto;
import com.caliper.location.dto.response.DealerList;
import com.caliper.location.dto.response.GMBAccountLocationsResponse;
import com.caliper.location.dto.response.ViewAllLocationsResponse;
import com.caliper.location.entity.Client;
import com.caliper.location.gmb.entity.GMBAccount;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.service.GMBLocationService;
import com.caliper.location.service.ClientService;
import com.caliper.location.service.LocationService;
import com.caliper.usermanagement.dto.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/locations")
@Tag(name = "Location", description = "Operations related to Locations and Clients")
public class LocationController {

	private static final Logger LOG = LoggerFactory.getLogger(LocationController.class);
	@Autowired
	private ClientService clientService;

	@Autowired
	private LocationService locationService;

	@Autowired
	private GMBLocationService gmbLocationService;

	@PostMapping("/create/client")
	@Operation(summary = "create client",
	responses = {
			@ApiResponse(
					responseCode = "200",
					description = "Client Created Successfully"

					), @ApiResponse(
							responseCode = "400",
							description = "Bad Request"
							)
	}
			)
	public ResponseEntity<?> createClient(@RequestBody Client client) {
		LOG.info("Creating client {}", client);
		clientService.insertClient(client);
		return ResponseEntity.ok().build();
	}


	@GetMapping("/get/client/{email}")
	@Operation(
			summary = "Get client by email",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Client fetched successfully"
							),
					@ApiResponse(
							responseCode = "404",
							description = "Client not found"
							)
			}
			)
	public Client findByEmail(@PathVariable String email) {
		return clientService.findByEmail(email);
	}

	@GetMapping("/get/client")
	@Operation(
			summary = "Get all clients",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "List of all clients fetched successfully"
							)
			}
			)
	public List<Client> getAllClients() {
		return clientService.getAllClients();
	}

	@GetMapping("/get/client/{id}")
	@Operation(
			summary = "Get client by id",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Client fetched successfully"
							),
					@ApiResponse(
							responseCode = "404",
							description = "Client not found"
							)
			}
			)
	public Optional<Client> getClientById(@PathVariable long id) {
		return clientService.findClientById(id);
	}

	@PostMapping("/view-all-locations")
	public ResponseEntity<ViewAllLocationsResponse> viewAllDealerLocations(@RequestBody UserDetails userDetails) {

		ViewAllLocationsResponse allLocations = locationService.getAllLocations(userDetails);

		return ResponseEntity.ok(allLocations);
	}
	
	@GetMapping("/fetch-gmb-account")
	public ResponseEntity<List<GMBAccount>> fetchGMBAccount(@RequestParam("client_id") String clientId) {
		return ResponseEntity.ok(gmbLocationService.fetchGMBAccount(clientId));
	}

	@PostMapping("/import-gmb-location")
	public ResponseEntity<GMBAccountLocationsResponse> importGMBLocation(@RequestBody List<GMBAccount> gmbAccounts) {

		GMBAccountLocationsResponse response = gmbLocationService.importGMBLocation(gmbAccounts);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/insert-gmb-location")
	@Operation(summary = "insert gmb location",
	responses = {
			@ApiResponse(
					responseCode = "200",
					description = "GMB Location Inserted Successfully"

					), @ApiResponse(
							responseCode = "400",
							description = "Bad Request"
							)
	}
			)
	public ResponseEntity<?> insertGMBLocation(@RequestBody List<GMBLocation> gmbLocList) {
		gmbLocationService.insertGMBLocation(gmbLocList);

		gmbLocationService.triggerReviewDeploymentAsync(gmbLocList.get(0).getClientId());
		gmbLocationService.triggerInsightDeploymentAsync(gmbLocList.get(0).getClientId());
		gmbLocationService.triggerMediaFetchAsync(gmbLocList.get(0).getClientId());
		return ResponseEntity.status(HttpStatus.OK).body("GMB Location Inserted Successfully");
	}



	@GetMapping("/get-dealer-list")
	public ResponseEntity<DealerList> fetchDealerListByClient(@RequestParam("client_id") String clientId,
			@RequestParam("user_id") String userId) {

		DealerList dealerList = locationService.fetchDealerListByClient(clientId, userId);

		return ResponseEntity.ok(dealerList);
	}

	//handle exceptions-provide proper exception response messages
	@PostMapping("/view-location-details")
	public ResponseEntity<LocationDetailsDto> viewLocationDetails(@RequestBody UserDetails userDetails) {

		//	ViewLocationDetails viewAllLocationDetails = locationService.fetchDealerLocationDetailsByDealer(userDetails);
		LocationDetailsDto locationDetailsDto = locationService.fetchDealerLocationDetailsByDealer(userDetails);
		return ResponseEntity.ok(locationDetailsDto);
	}

	//handle multiple exceptions, and send it as response. define runtime exception, and add in GlobalExceptionHandler
	@PostMapping("/update/{dealerId}")
	public ResponseEntity<String> updateLocation(
			@PathVariable("dealerId") String dealerId,
			@RequestParam("clientId") String clientId,
			@RequestParam("userId") String userId,
			@RequestBody InfoRequestDto request) {

		locationService.insertRequestIntoInfoQueue(dealerId, clientId, userId, request);

		return ResponseEntity
				.status(HttpStatus.OK)
				.body("Data updated successfully for dealer id - " + dealerId);

	}

	@PostMapping("/update-campaign-settings/{dealerId}")
	public ResponseEntity<String> updateCampaignSettings(
			@PathVariable("dealerId") String dealerId,
			@RequestParam("clientId") String clientId,
			@RequestBody CampaignSettingsBody campaignSettingsRequest) {

		locationService.updateCampaignSettings(dealerId, clientId, campaignSettingsRequest);

		return ResponseEntity
				.status(HttpStatus.OK)
				.body("Campaign Settings updated successfully for client id - " + clientId);

	}

	@PostMapping("/update-gmb-details/{dealerId}")
	public ResponseEntity<String> updateGMBDetails(
			@PathVariable("dealerId") String dealerId,
			@RequestParam("clientId") String clientId,
			@RequestBody GMBDetailsBody gmbDetailsBody) {

		locationService.updateGMBDetails(dealerId, clientId, gmbDetailsBody);

		return ResponseEntity
				.status(HttpStatus.OK)
				.body("GMB details updated successfully for client id - " + clientId);

	}

	@PostMapping("/update-facebook-details/{dealerId}")
	public ResponseEntity<String> updateFBDetails(
			@PathVariable("dealerId") String dealerId,
			@RequestParam("clientId") String clientId,
			@RequestBody FacebookDetailsBody fbDetailsDto) {

		locationService.updatefacebookDetails(dealerId, clientId, fbDetailsDto);

		return ResponseEntity
				.status(HttpStatus.OK)
				.body("Facebook details updated successfully for client id - " + clientId);

	}

	@PostMapping("/update-location-overview-details/{dealerId}")
	public ResponseEntity<String> updateOverviewDetails(
			@PathVariable("dealerId") String dealerId,
			@RequestParam("clientId") String clientId,
			@RequestBody LocationOverviewBody locationOverviewBody) {

		locationService.updateLocationOverviewDetails(dealerId, clientId, locationOverviewBody);

		return ResponseEntity
				.status(HttpStatus.OK)
				.body("Location overview details updated successfully for client id - " + clientId);

	}

	@GetMapping("/get-campaign-settings/{dealerId}")
	public ResponseEntity<CampaignSettings> fetchCampaignSettings(
			@PathVariable("dealerId") String dealerId,
			@RequestParam("clientId") String clientId,
			@RequestParam("userId") String userId) {

		CampaignSettings campaignSettings = locationService.fetchCampaignSettingsByDealerIdAndClientId(dealerId, clientId, userId);

		return ResponseEntity.ok(campaignSettings) ;
	}

	@PostMapping("/group-exist")
	public ResponseEntity<AccountVerifyResponseDto> isGroupExists(@RequestBody Map<String, String> request) throws Exception {

		String groupName = request.get("groupName");
		AccountVerifyResponseDto exists = locationService.isGroupExists(groupName);

		return ResponseEntity.ok(exists);
	}

	@GetMapping("/get-categories")
	public ResponseEntity<List<CategoryDto>>fetchCategories(@RequestParam("source") String source){

		List<CategoryDto> categoryDto = locationService.fetchCategories(source);

		return ResponseEntity.ok(categoryDto);
	}
}

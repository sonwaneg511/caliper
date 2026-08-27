package com.caliper.task;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.bigQuery.GMBLocationBQRepository;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.gmb.dto.GMBInfoRequest;
import com.caliper.location.gmb.entity.GMBAccount;
import com.caliper.location.gmb.entity.GMBInfoQueue;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.entity.GMBOperationHours;
import com.caliper.location.gmb.repository.GMBInfoQueueRepository;
import com.caliper.location.gmb.repository.GMBOperationHoursRepository;
import com.caliper.location.gmb.service.GMBAccountService;
import com.caliper.location.gmb.service.GMBInfoQueueService;
import com.caliper.location.gmb.service.GMBLocationService;
import com.caliper.location.gmb.service.GMBSessionFactory;
import com.caliper.location.entity.Client;
import com.caliper.location.service.ClientService;
import com.caliper.location.service.DealerLocationService;
import com.caliper.location.service.LocationService;
import com.google.api.services.mybusinessbusinessinformation.v1.MyBusinessBusinessInformation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.jsonwebtoken.io.IOException;
@Service
public class GMBDeploymentTask extends ParameterizedJob{
	private boolean fetchLocations;
	private boolean fetchLocationStatus;
	private boolean importGMBLocations;
	private boolean insertBQ;
	private boolean updateInfo;
	private boolean importDealerLocation;
	private List<Client> clientList;

	@Autowired
	private ClientService clientService;

	@Autowired
	private GMBAccountService gmbAccountService;

	@Autowired
	private GMBOperationHoursRepository gmbOperationHoursRepository;

	@Autowired
	private GMBLocationService gmbLocationService;

	@Autowired
	private GMBInfoQueueRepository gmbInfoQueueRepository; 

	@Autowired
	private GMBInfoQueueService gmbInfoQueueService;

	@Autowired
	private GMBSessionFactory gmbSessionFactory;

	@Autowired
	private LocationService locationService;

	@Autowired
	private GMBLocationBQRepository gmbLocationBQRepository;

	@Autowired
	private DealerLocationService dealerLocationService;

	@Override
	public void run() {

		try {
			init();
			fetchLocations();
			updateInfo();
		}	catch (Exception e) {
			e.printStackTrace();
			log(e.toString());
			throw new RuntimeException("Error in run()", e);
		}
	}

	private void init() {
		this.fetchLocations = parameters.getBoolean("fetch-locations",false);
		this.importGMBLocations = parameters.getBoolean("import-locations",false);
		this.updateInfo = parameters.getBoolean("update-info",false);
		this.fetchLocationStatus = parameters.getBoolean("fetch-location-status",false);
		this.insertBQ = parameters.getBoolean("insert-BQ",false);
		this.importDealerLocation = parameters.getBoolean("import-dealer-location",false);

		String clientId = parameters.getString("client-id", null);
		if (clientId != null && !clientId.isBlank()) {
			this.clientList = List.of(clientService.findByClientId(clientId));
		} else {
			this.clientList = clientService.findAllClient();
		}
	}

	private void fetchLocations() throws Exception {

		if (!fetchLocations && !fetchLocationStatus) {
			return;
		}
		try {

			for (Client client : clientList) {

				String clientId = client.getClientId();
				log("Processing client - "+clientId);

				List<GMBAccount> gmbAccounts = gmbAccountService.findByClientId(clientId);

				for(GMBAccount gmbAccount : gmbAccounts) {

					log("Processing account - "+gmbAccount.getAccountName());

					List<GMBLocation> dbGmbLocations = gmbLocationService.getAllGMBLocationsByAccountId(gmbAccount.getAccountId());
					List<DealerLocation> dealerLocations = dealerLocationService.getAllDealerLocationsByClientId(clientId);
					List<GMBOperationHours> allGMBOperationHours = gmbOperationHoursRepository.findByClientId(clientId);

					Map<String, GMBLocation> gmbLocationIdVsDbLocationMap = dbGmbLocations.stream()
							.collect(Collectors.toMap(GMBLocation::getGmbLocationId, Function.identity()));

					Map<String, GMBLocation> gmbDealerIdVsDbLocationMap = dbGmbLocations.stream()
							.filter(loc -> StringUtils.isNotBlank(loc.getDealerId()))
							.collect(Collectors.toMap(GMBLocation::getDealerId, Function.identity()));

					Set<String> dealerlocationIds = dealerLocations.stream()
							.map(DealerLocation::getDealerId)
							.collect(Collectors.toSet());

					Set<String> operationlocationIds = allGMBOperationHours.stream()
							.map(GMBOperationHours::getGmbLocationId)
							.collect(Collectors.toSet());

					gmbLocationService.processFetchLocations(clientId, gmbAccount, fetchLocationStatus, importGMBLocations, importDealerLocation, new ArrayList<>(), gmbLocationIdVsDbLocationMap, gmbDealerIdVsDbLocationMap, operationlocationIds, dealerlocationIds, 10);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			log("Error in fetch locations task");
			log(e.getStackTrace().toString());
		}
	}

	//add exception handling
	private void updateInfo() throws SQLException, IOException, FileNotFoundException, java.io.IOException {

		if (!updateInfo) {
			return;
		}
		try {
			MyBusinessBusinessInformation businessLocation = gmbSessionFactory.getGMBLocationSession();

			List<GMBInfoQueue> allGMBInfoQueue = gmbInfoQueueService.findByStatus(GMBInfoQueue.STATUS_SUBMIT);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			if (allGMBInfoQueue == null || allGMBInfoQueue.isEmpty()) {
				log("No GMBInfoQueue entries to process.");
				return;
			}


			for (GMBInfoQueue gmbInfo : allGMBInfoQueue) {
				try {
					String clientId = gmbInfo.getClientId();
					String dealerId = gmbInfo.getDealerId();

					GMBLocation gmbLocation = gmbLocationService.getAllGmbLocationByClientIdAndDealerId(clientId, dealerId);

					if (gmbLocation == null) {
						log("No GMBLocation found for dealerId=" + dealerId);
						markError(gmbInfo.getId());
						continue;
					}

					GMBInfoRequest info = gson.fromJson(gmbInfo.getRequestJson(), GMBInfoRequest.class);

					log("Updating Info Id : " + gmbInfo.getId());
					Thread.sleep(100);
					//add log object
					gmbLocation = locationService.updateGMBLocationInfo(clientId, businessLocation, gmbLocation, info, null, false);

					if (gmbLocation != null) {
						markDeployed(gmbInfo.getId());
					}else {
						log("Update returned null for Info Id: " + gmbInfo.getId());
						markError(gmbInfo.getId());
					}
				} catch (Exception e) {
					e.printStackTrace();
					log("Error deploying info - " + gmbInfo.getId() + " : Error Message - " + e.getMessage());
					markError(gmbInfo.getId());
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			log("Error in update info task");
			log(e.getStackTrace().toString());
		}
	}

	private void markError(Long gmbInfoQueueId) {
		try {
			gmbInfoQueueRepository.updateGMBInfoQueueStatusById(GMBInfoQueue.STATUS_ERROR, gmbInfoQueueId);
			log("Marked ERROR for GMBInfoQueue id=" + gmbInfoQueueId);
		} catch (Exception ex) {
			log("Failed to mark ERROR for GMBInfoQueue id=" + gmbInfoQueueId + ", error=" + ex.getMessage());
		}
	}

	private void markDeployed(Long gmbInfoId) {
		try {
			gmbInfoQueueRepository.updateGMBInfoQueueStatusById(GMBInfoQueue.STATUS_DEPLOYED, gmbInfoId);
			log("Marked DEPLOYED for GMBInfoQueue id=" + gmbInfoId);
		} catch (Exception ex) {
			log("Failed to mark DEPLOYED for GMBInfoQueue id=" + gmbInfoId + ", error=" + ex.getMessage());
		}
	}

}
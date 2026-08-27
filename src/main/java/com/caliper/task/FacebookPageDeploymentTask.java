package com.caliper.task;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.facebook.dto.FacebookInfoRequest;
import com.caliper.location.facebook.entity.FacebookInfoQueue;
import com.caliper.location.facebook.entity.FacebookLocation;
import com.caliper.location.facebook.entity.FacebookPage;
import com.caliper.location.facebook.service.FacebookInfoQueueService;
import com.caliper.location.facebook.service.FacebookLocationService;
import com.caliper.location.facebook.service.FacebookPageService;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.service.GMBLocationService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.jsonwebtoken.io.IOException;
@Service
public class FacebookPageDeploymentTask extends ParameterizedJob{

	private boolean fetchLocations;
	private boolean updateInfo;

	@Autowired
	private FacebookPageService facebookPageService;

	@Autowired
	private FacebookLocationService facebookLocationService;

	@Autowired
	private GMBLocationService gmbLocationService;

	@Autowired
	private FacebookInfoQueueService facebookInfoQueueService;

	@Override
	public void run() {
		init();	
		try {
			fetchLocations();
			updateInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void init() {
		this.fetchLocations = parameters.getBoolean("fetch-locations",false);
		this.updateInfo = parameters.getBoolean("update-info",false);
	}

	private void fetchLocations() throws Exception {

		if (!fetchLocations) {
			return;
		}
		try {

			List<FacebookPage> facebookPageAccounts = facebookPageService.getAllFacebookPages();

			for(FacebookPage facebookPageAccount : facebookPageAccounts) {

				String clientId = facebookPageAccount.getClientId();
				List<FacebookLocation> dbFacebookLocations = facebookLocationService.getAllFacebookLocationsByClientId(clientId);
				List<FacebookLocation> facebookLocationsDB = dbFacebookLocations;
				Map<String, FacebookLocation> facebookPageIdVsDbLocationMap = new HashMap<String, FacebookLocation>();
				Map<String, GMBLocation> gmbLocationDealerIdVsDbLocationMap = new HashMap<String, GMBLocation>();
				List<GMBLocation> gmbLocations = gmbLocationService.getAllGmbLocationByClientId(clientId);
				for(GMBLocation gmbLocation : gmbLocations) {
					gmbLocationDealerIdVsDbLocationMap.put(gmbLocation.getDealerId(), gmbLocation);
				}

				for (FacebookLocation fbPagesDb : facebookLocationsDB) {
					facebookPageIdVsDbLocationMap.put(fbPagesDb.getFacebookPageId(), fbPagesDb);
				}

				facebookLocationService.processFacebookFetchLocations(clientId, fetchLocations, facebookPageAccount, gmbLocationDealerIdVsDbLocationMap,
						facebookPageIdVsDbLocationMap);

			}
		}catch (Exception e) {
			e.printStackTrace();
			log("Error in fetch locations task");
			log(e.getStackTrace().toString());
		}
	}

	private void updateInfo() throws SQLException, IOException {

		if (!updateInfo) {
			return;
		}

		try {
		
		List<FacebookInfoQueue> allFBInfoQueue = facebookInfoQueueService.findByStatus(FacebookInfoQueue.STATUS_SUBMIT);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		if (allFBInfoQueue == null || allFBInfoQueue.isEmpty()) {
			log("No Facebook InfoQueue entries to process.");
			return;
		}

		for (FacebookInfoQueue fbInfo : allFBInfoQueue) {
			try {
				String clientId = fbInfo.getClientId();
				String fbPageId = fbInfo.getFacebookPageId();

				FacebookLocation fbLocation = facebookLocationService.getFacebookLocationByClientIdAndLocationId(clientId, fbPageId);

				if (fbLocation == null) {
					log("No FacebookLocation found for pageId=" + fbPageId);
					markFacebookError(fbInfo.getId());
					continue;
				}

				FacebookInfoRequest info = gson.fromJson(fbInfo.getRequestJson(), FacebookInfoRequest.class);

				log("Updating Info Id: " + fbInfo.getId());
				Thread.sleep(100);

				fbLocation = facebookInfoQueueService.updateFacebookLocationInfo(clientId, fbLocation, info, null, false);

				if (fbLocation != null) {
					markFacebookDeployed(fbInfo.getId());
				} else {
					log("Update returned null for Info Id: " + fbInfo.getId());
					markFacebookError(fbInfo.getId());
				}

			} catch (Exception e) {
				e.printStackTrace();
				log("Error deploying info - ID: " + fbInfo.getId() +
						", Exception: " + e.getClass().getSimpleName() +
						", Message: " + e.getMessage());
				markFacebookError(fbInfo.getId());
			}
		}
		}catch (Exception e) {
			e.printStackTrace();
			log("Error in update info task");
			log(e.getStackTrace().toString());
		}
	}
	private void markFacebookError(Long fbInfoId) {
		try {
			facebookInfoQueueService.updateFacebookLocationInfoStatus(FacebookInfoQueue.STATUS_ERROR, fbInfoId);
			log("Marked ERROR for FacebookInfoQueue id=" + fbInfoId);
		} catch (Exception ex) {
			log("Failed to mark ERROR for FacebookInfoQueue id=" + fbInfoId +
					", Exception: " + ex.getClass().getSimpleName() +
					", Message: " + ex.getMessage());
		}
	}

	private void markFacebookDeployed(Long fbInfoId) {
		try {
			facebookInfoQueueService.updateFacebookLocationInfoStatus(FacebookInfoQueue.STATUS_DEPLOYED, fbInfoId);
			log("Marked DEPLOYED for FacebookInfoQueue id=" + fbInfoId);
		} catch (Exception ex) {
			log("Failed to mark DEPLOYED for FacebookInfoQueue id=" + fbInfoId +
					", Exception: " + ex.getClass().getSimpleName() +
					", Message: " + ex.getMessage());
		}
	}
}
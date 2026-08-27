package com.caliper.location.gmb.service;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.caliper.location.specification.DealerLocationSepcification;
import com.caliper.location.specification.GMBLocationSpecification;
import com.caliper.planmanagement.repository.PlanRepository;
import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.repository.UserClientLocMappingRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.google.api.services.mybusiness.v4.model.Account;
import com.google.api.services.mybusinessbusinessinformation.v1.MyBusinessBusinessInformation;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Categories;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Category;
import com.google.api.services.mybusinessbusinessinformation.v1.model.ListLocationsResponse;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Location;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Metadata;
import com.google.api.services.mybusinessbusinessinformation.v1.model.PostalAddress;
import com.google.api.services.mybusinessverifications.v1.MyBusinessVerifications;
import com.google.api.services.mybusinessverifications.v1.model.VoiceOfMerchantState;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import lombok.extern.slf4j.Slf4j;

import com.caliper.location.controller.LocationController;
import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.dto.response.GMBAccountLocationsResponse;
import com.caliper.location.entity.Client;
import com.caliper.onboarding.service.OnboardingService;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.gmb.entity.GMBAccount;
import com.caliper.location.gmb.entity.GMBCategory;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.entity.GMBOperationHours;
import com.caliper.location.gmb.entity.GMBSettings;
import com.caliper.location.gmb.repository.GMBAccountRepository;
import com.caliper.location.gmb.repository.GMBCategoryRepository;
import com.caliper.location.gmb.repository.GMBLocationRepository;
import com.caliper.location.gmb.repository.GMBOperationHoursRepository;
import com.caliper.location.gmb.repository.GMBSettingsRepository;
import com.caliper.location.repository.ClientRepository;
import com.caliper.location.repository.DealerLocationRepository;
import com.caliper.location.service.DealerLocationService;
import com.caliper.planmanagement.entity.Plan;
import com.caliper.reporting.entity.GMBInsightSettings;
import com.caliper.reporting.service.GMBInsightService;
import com.caliper.review.entity.ReviewSettings;
import com.caliper.review.repository.ReviewSettingsRepository;
import com.caliper.task.GMBInsightDeploymentTask;
import com.caliper.task.GMBMediaFetchTask;
import com.caliper.task.GMBReviewDeployementTask;
@Slf4j
@Service
public class GMBLocationService {

	@Autowired
	private GMBLocationRepository gmbLocationRepository;

	@Autowired
	private GMBCategoryService gmbCategoryService;

	@Autowired
	private GMBOperationHoursService gmbOperationHoursService;

	@Autowired
	private GMBCategoryRepository gmbCategoryRepository;

	@Autowired
	private GMBSettingsRepository gmbSettingsRepository;

	@Autowired
	private GMBSessionFactory gmbSessionFactory;

	@Autowired
	private DealerLocationRepository dealerLocationRepository;

	@Autowired
	private DealerLocationService dealerLocationService;

	@Autowired
	private GMBOperationHoursRepository gmbOperationHoursRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private UserClientLocMappingRepository userClientLocMappingRepository;


	@Autowired
	private GMBAccountRepository gmbAccountRepository;

	@Autowired
	private PlanRepository planRepository;

	@Autowired
	private OnboardingService onboardingService;

	@Autowired
	@Qualifier("gmbImportPreviewExecutor")
	private Executor gmbImportPreviewExecutor;

	@Autowired
	@Lazy
	private GMBReviewDeployementTask gmbReviewDeployementTask;

	@Autowired
	@Lazy
	private GMBInsightDeploymentTask gmbInsightDeploymentTask;

	@Autowired
	@Lazy
	private GMBMediaFetchTask gmbMediaFetchTask;

	@Autowired
	private ReviewSettingsRepository reviewSettingsRepository;

	@Autowired
	private GMBInsightService gmbInsightService;

	private static final Date SEED_REVIEW_AUTO_RESPONSE_DATE = new GregorianCalendar(2000, 0, 1).getTime();
	private static final Date SEED_INSIGHT_LAST_INSERTED_DATE = new GregorianCalendar(2024, 0, 1).getTime();

	private static final Logger LOG = LoggerFactory.getLogger(LocationController.class);

	@Value("${file.upload.dir}")
	public static String fileUploadDir;

	@Value("${server.path}")
	public static String serverPath;

	@Value("${log.path}")
	private String logPath;

	public List<GMBLocation> getFilteredGmbLocation(LocationFilterRequest req){
		List<GMBLocation> locations = gmbLocationRepository.findAll(GMBLocationSpecification.filterLocations(req));
		return locations;
	}

	public List<GMBLocation>getAllGMBLocations(){
		return gmbLocationRepository.findAll();

	}

	public List<GMBLocation>getAllGMBLocationsByAccountId(String accountId){
		return gmbLocationRepository.findByAccountId(accountId);

	}

	public List<GMBLocation>getAllGMBLocationsByAccountIdAndClientId(String accountId, String clientId){
		return gmbLocationRepository.findByAccountIdAndClientId(accountId, clientId);

	}

	public GMBLocation getAllGmbLocationByClientIdAndDealerId(String clientId, String dealerId) {
		return gmbLocationRepository.getAllGmbLocationByClientIdAndDealerId(clientId, dealerId);

	}

	public List<GMBLocation> getAllGmbLocationByClientId(String clientId) {
		return gmbLocationRepository.getAllGmbLocationByClientId(clientId);

	}

	public void updatePlaceActionIdAndAppointmentLinkByDealerId(String dealerId, String placeActionId, String appointmentLink){
		gmbLocationRepository.updatePlaceActionAndAppointmentLink(placeActionId, appointmentLink, dealerId);
	}

	public void updateAttributesByDealerId(String whatsAppUrl, String instagramUrl, String facebookUrl, String twitterUrl, String youtubeUrl, String linkedinUrl, String dealerId) {
		gmbLocationRepository.updateAttributesByDealerId(whatsAppUrl, instagramUrl, facebookUrl, twitterUrl, youtubeUrl, linkedinUrl, dealerId);
	}

	public GMBLocation getLocation(GMBLocation gmbLocation, boolean getLocationStatus) throws Exception {
		GMBLocation newGmbLocation = null;
		List<GMBCategory> gmbCategories = gmbCategoryRepository.findAll();
		Map<String,Long> categoryMap = new HashMap<>();
		for(GMBCategory category : gmbCategories) {
			categoryMap.put(category.getCategoryId(), category.getId());
		}

		String[] split = gmbLocation.getGmbLocationId().split("/");
		String gmbLocationID = split[2]+"/"+split[3];

		MyBusinessVerifications verificationSession = getLocationStatus
				? gmbSessionFactory.getGMBLocationVerificationSession(gmbLocation.getClientId())
						: null;

		for (int i = 0; i < 4; i++) {
			try {
				if (i > 0) {
					Thread.sleep(2000);
				}
				MyBusinessBusinessInformation business = gmbSessionFactory.getGMBLocationSession();
				Location location = business.locations().get(gmbLocationID)
						.setReadMask("title,name,storeCode,regularHours,labels,latlng,storefrontAddress,profile,metadata,websiteUri,phoneNumbers,categories,openInfo")
						.execute();
				if(location != null) {
					newGmbLocation = getGMBLocation(location, gmbLocation.getAccountId(), categoryMap, getLocationStatus, verificationSession);
				}
				break;
			} catch (Exception ex) {
				LOG.error("Error fetching location (attempt {}), retrying in 5s: {}", i + 1, ex.getMessage(), ex);
				Thread.sleep(5000);
			}
		}

		return newGmbLocation;
	}

	public List<GMBLocation> getLocations(GMBAccount gmbAccount, boolean getLocationStatus) throws Exception {
		boolean getNextPage = true;
		String pageToken = "";
		int processed = 0;

		List<GMBLocation> gmbLocations = new ArrayList<>();
		ListLocationsResponse locations =  null;

		List<GMBCategory> gmbCategories = gmbCategoryService.getAllGMBCategory();
		Map<String,Long> categoryMap = new HashMap<>();
		for(GMBCategory category : gmbCategories) { 
			categoryMap.put(category.getCategoryId(), category.getId());
		}

		MyBusinessBusinessInformation business = gmbSessionFactory.getGMBLocationSession(gmbAccount.getClientId());
		MyBusinessVerifications verificationSession = getLocationStatus
				? gmbSessionFactory.getGMBLocationVerificationSession(gmbAccount.getClientId())
						: null;

		while(getNextPage) {
			for (int i = 0; i < 4; i++) {
				try {
					if (i > 0) {
						Thread.sleep(2000);
					}
					System.out.println("Page Token - " + pageToken);

					locations = business.accounts().locations().list(gmbAccount.getAccountId())
							.setReadMask("title,name,storeCode,regularHours,labels,latlng,storefrontAddress,profile,metadata,websiteUri,phoneNumbers,categories,openInfo").setPageSize(100).setPageToken(pageToken).execute();

					List<Location> locations2 = locations.getLocations();
					if (locations2 == null) {
						locations2 = Collections.emptyList();
					}
					for(Location location : locations2) {

						if(location != null) {
							GMBLocation gmbLocation = getGMBLocation(location, gmbAccount.getAccountId(), categoryMap, getLocationStatus, verificationSession);
							gmbLocations.add(gmbLocation);
						}

						processed++;

					}

					pageToken = locations.getNextPageToken();
					if(pageToken == null) {
						getNextPage = false;
					}
					break;
				} catch (Exception ex) {
					LOG.error("Error fetching locations (attempt {}), retrying in 5s: {}", i + 1, ex.getMessage(), ex);
					Thread.sleep(5000);
				}
			}
		}

		return gmbLocations;
	}

	public List<GMBLocation> getLocationsViaGMBOAUTH(Account gmbAccount, boolean getLocationStatus, String clientId) throws Exception {
		boolean getNextPage = true;
		String pageToken = "";
		int processed = 0;

		List<GMBLocation> gmbLocations = new ArrayList<>();
		ListLocationsResponse locations =  null;

		List<GMBCategory> gmbCategories = gmbCategoryService.getAllGMBCategory();
		Map<String,Long> categoryMap = new HashMap<>();
		for(GMBCategory category : gmbCategories) { 
			categoryMap.put(category.getCategoryId(), category.getId());
		}

		MyBusinessBusinessInformation business = gmbSessionFactory.getGMBLocationSession(clientId);
		MyBusinessVerifications verificationSession = getLocationStatus
				? gmbSessionFactory.getGMBLocationVerificationSession(clientId)
						: null;

		while(getNextPage) {
			for (int i = 0; i < 4; i++) {
				try {
					if (i > 0) {
						Thread.sleep(2000);
					}
					System.out.println("Page Token - " + pageToken);

					locations = business.accounts().locations().list(gmbAccount.getName())
							.setReadMask("title,name,storeCode,regularHours,labels,latlng,storefrontAddress,profile,metadata,websiteUri,phoneNumbers,categories,openInfo").setPageSize(100).setPageToken(pageToken).execute();

					List<Location> locations2 = locations.getLocations();
					if (locations2 == null) {
						locations2 = Collections.emptyList();
					}
					for(Location location : locations2) {

						if(location != null) {
							GMBLocation gmbLocation = getGMBLocation(location, gmbAccount.getName(), categoryMap, getLocationStatus, verificationSession);
							gmbLocations.add(gmbLocation);
						}

						processed++;

					}

					pageToken = locations.getNextPageToken();
					if(pageToken == null) {
						getNextPage = false;
					}
					break;
				} catch (Exception ex) {
					LOG.error("Error fetching locations (attempt {}), retrying in 5s: {}", i + 1, ex.getMessage(), ex);
					Thread.sleep(5000);
				}
			}
		}

		return gmbLocations;
	}

	public GMBLocation getGMBLocation(Location location, String accountId, Map<String,Long> categoryMap, boolean getLocationStatus, MyBusinessVerifications verificationSession) throws SQLException, IOException{
		PostalAddress address = location.getStorefrontAddress();
		String addr = "";
		String area = "";
		String city = "";
		String state = "";
		long pincode = 0;
		String latitude = "";
		String longitude = "";
		String countryCode = "";
		String languageCode = "";
		String addressLine1 = "";
		String addressLine2 = "";
		String addressLine3= "";
		String country = "";

		if(address != null && !address.isEmpty()) {
			List<String> addressLines = address.getAddressLines();
			if(addressLines != null && !addressLines.isEmpty()) {
				for (String line : addressLines) {
					if (line != null) {
						addr += " " + line;
					} 
				}

				if(addressLines.size()>0) {
					addressLine1 = addressLines.remove(0);
				}
				if(addressLines.size()>0) {
					addressLine2 = addressLines.remove(0);
				}

				for(String add:addressLines) {
					addressLine3 += " " + add;
				}
			}
			addr = addr.trim();

			//addr = address.getAddressLines().get(0);
			area = address.getSublocality() == null ? "" : address.getSublocality();
			city = address.getLocality();
			state = address.getAdministrativeArea();
			pincode = (address.getPostalCode() == null) ? 0 : Long.parseLong(address.getPostalCode());
			latitude = (location.getLatlng() == null) ? "" : String.valueOf(location.getLatlng().getLatitude());
			longitude = (location.getLatlng() == null) ? "" : String.valueOf(location.getLatlng().getLongitude());
			countryCode = location.getStorefrontAddress().getRegionCode();
			country= GMBHelper.getCountryByCode(countryCode);
			languageCode = location.getStorefrontAddress().getLanguageCode();
		}
		String website = (location.getWebsiteUri() == null) ? "" : location.getWebsiteUri() ;
		String description = (location.getProfile() == null) ? "" : (location.getProfile().getDescription() == null || location.getProfile().getDescription().equalsIgnoreCase("")) ? "" : location.getProfile().getDescription();

		Categories categories = location.getCategories();
		List<Category> additionalCategories = categories.getAdditionalCategories();
		String additionalCategoriesId = "";
		if(additionalCategories != null && additionalCategories.size() > 0) {
			for(Category category : additionalCategories) {
				String categoryName = category.getName();
				if(categoryName.contains("/")) {
					categoryName = categoryName.split("/")[1];
				}
				System.out.println("category Name = "+categoryName);
				long id = 0;
				if(categoryMap.containsKey(categoryName)) {
					id = categoryMap.get(categoryName);
				}
				additionalCategoriesId = additionalCategoriesId + id + ",";
			}
			if(StringUtils.isNotEmpty(additionalCategoriesId)){
				int index = additionalCategoriesId.lastIndexOf(",");
				additionalCategoriesId = additionalCategoriesId.substring(0, index);
			}
		}
		String labels = "";

		if(location.getLabels() != null) {
			for(String label : location.getLabels()) {
				labels +=label + " | ";
			}
			if(labels.endsWith("| ")) {
				labels = labels.substring(0, labels.length()-2);
			}
		}
		String storeCode = (location.getStoreCode() == null) ? "" : location.getStoreCode();
		String status = "NA";
		//boolean hasVoiceOfMerchantObj = location.getMetadata() == null ? false : location.getMetadata().getHasVoiceOfMerchant() == null ? false : location.getMetadata().getHasVoiceOfMerchant();
		if(getLocationStatus) {
			String duplicateLocation = location.getMetadata().getDuplicateLocation();
			//		boolean hasVoiceOfMerchant = false;
			//		boolean hasBusinessAuthority = false;
			String recommendationReason = null;
			boolean hasPendingVerification = false;

			//if(hasVoiceOfMerchantObj == false) {
			VoiceOfMerchantState voiceOfMerchant = verificationSession.locations().getVoiceOfMerchantState(location.getName()).execute();
			//			hasVoiceOfMerchant = voiceOfMerchant.getHasVoiceOfMerchant() == null ? false : voiceOfMerchant.getHasVoiceOfMerchant();
			//			hasBusinessAuthority = voiceOfMerchant.getHasBusinessAuthority() == null ? false : voiceOfMerchant.getHasBusinessAuthority();
			recommendationReason =  voiceOfMerchant.getComplyWithGuidelines() == null ? null : voiceOfMerchant.getComplyWithGuidelines().getRecommendationReason() == null ? null : voiceOfMerchant.getComplyWithGuidelines().getRecommendationReason();
			hasPendingVerification = voiceOfMerchant.getVerify() == null ? false : voiceOfMerchant.getVerify().getHasPendingVerification() == null ? false : voiceOfMerchant.getVerify().getHasPendingVerification();
			//}

			/*	System.out.println("Processing store code : "+location.getStoreCode());
			System.out.println("voiceOfMerchant :: "+voiceOfMerchant);
			System.out.println("Store code :: "+storeCode +" duplicateLocation :: "+duplicateLocation+" recommendationReason :: "+recommendationReason+" hasPendingVerification :: "+hasPendingVerification);*/
			if (duplicateLocation != null && !duplicateLocation.equalsIgnoreCase("")) {
				status = GMBLocation.LOCATION_STATE_DUPLICATE;
			}else if(recommendationReason != null && recommendationReason.equalsIgnoreCase(GMBLocation.LOCATION_STATE_SUSPENDED)) {
				status = GMBLocation.LOCATION_STATE_SUSPENDED;
			}else if (recommendationReason != null && recommendationReason.equalsIgnoreCase(GMBLocation.LOCATION_STATE_DISABLED)) {
				status = GMBLocation.LOCATION_STATE_DISABLED;
			}else if (hasPendingVerification == false && recommendationReason == null) {
				status = GMBLocation.LOCATION_STATE_VERIFIED;
			}else {
				status = GMBLocation.LOCATION_STATE_UNVERIFIED;
			}
			System.out.println("status of store code : "+storeCode +"- "+ status);
		}

		String placeActionId = "";
		String appointmentLink = "";
		String openInfoStatus = location.getOpenInfo() == null ? "NA" : location.getOpenInfo().getStatus() == null || location.getOpenInfo().getStatus().equalsIgnoreCase("") ? "NA" :location.getOpenInfo().getStatus();
		boolean canReopen = location.getOpenInfo() == null ? false : location.getOpenInfo().getCanReopen() == null || location.getOpenInfo().getCanReopen().equals(null) ? false : location.getOpenInfo().getCanReopen();

		String primaryPhone = (location.getPhoneNumbers().getPrimaryPhone() == null) ? "" : location.getPhoneNumbers().getPrimaryPhone();
		List<String> additionalPhones = location.getPhoneNumbers() == null ? null : location.getPhoneNumbers().getAdditionalPhones() == null ? null : location.getPhoneNumbers().getAdditionalPhones();


		String additionalPhoneNos = "";
		if(additionalPhones != null && additionalPhones.size() > 0) {
			for(String no : additionalPhones) {
				additionalPhoneNos += no +",";
			}

			if(additionalPhoneNos.endsWith(",")) {
				additionalPhoneNos = additionalPhoneNos.substring(0, additionalPhoneNos.length()-1);
			}
		}
		String primarCategoryName = categories.getPrimaryCategory() == null  ? "NA" : categories.getPrimaryCategory().getName();
		String primarCategoryId = null;
		if(primarCategoryName.contains("/")) {
			primarCategoryId = primarCategoryName.split("/")[1];
		}
		Metadata metadata = (location.getMetadata() == null) ? null : location.getMetadata();
		String mapsUrl = "";
		String newReviewUrl = "";
		if(metadata != null) {
			mapsUrl = location.getMetadata().getMapsUri() == null || location.getMetadata().getMapsUri().equalsIgnoreCase("") ? "" : location.getMetadata().getMapsUri();
			newReviewUrl = location.getMetadata().getNewReviewUri() == null || location.getMetadata().getNewReviewUri().equalsIgnoreCase("")  ? "" : location.getMetadata().getNewReviewUri();
		}

		if(primarCategoryId != null && primarCategoryId.equalsIgnoreCase("gcid:shopping_destination")) {
			primarCategoryId = "gcid:shopping_center";
		}
		if(primarCategoryId == null || primarCategoryId.equalsIgnoreCase("")) {
			primarCategoryId = "0";
		}

		long primaryId = 0;
		if(categoryMap.containsKey(primarCategoryId)) {
			primaryId = categoryMap.get(primarCategoryId);
		}


		/*	GMBLocation gmbLocation = new GMBLocation(-1L, accountId, accountId + "/"+location.getName(), storeCode, location.getTitle(), addr, area, city,
				state, pincode, latitude, longitude, countryCode, languageCode, primaryPhone, additionalPhoneNos,
				mapsUrl, newReviewUrl, canReopen, openInfoStatus, website, description, primaryId,
				additionalCategoriesId, labels, status, placeActionId, appointmentLink, addressLine1, addressLine2, addressLine3, new Date(), "", "", "", "", "","");
		 */
		GMBLocation gmbLocation = GMBLocation.builder()
				.gmbLocationId(accountId.trim() +"/"+ location.getName().trim())
				.accountId(accountId)
				//  .clientId(clientId) // if you have it
				.dealerId(storeCode)
				.name(location.getTitle())
				.address(addr)
				.area(area)
				.city(city)
				.state(state)
				.pincode(pincode)
				.latitude(latitude)
				.longitude(longitude)
				.countryCode(countryCode)
				.languageCode(languageCode)
				.phoneNumber(primaryPhone)
				.additionalPhones(additionalPhoneNos)
				.mapUrl(mapsUrl)
				.newReviewUrl(newReviewUrl)
				.openInfoCanReopen(canReopen)
				.openInfoStatus(openInfoStatus)
				.websiteUrl(website)
				.description(description)
				.primaryCategory(primaryId)
				.additionalCategories(additionalCategoriesId)
				.labels(labels)
				.status(status)
				.placeActionId(placeActionId)
				.appointmentLink(appointmentLink)
				.address1(addressLine1)
				.address2(addressLine2)
				.address3(addressLine3)
				.insertedDate(new Date())
				.whatsappUrl("")   
				.instagramUrl("") 
				.youtubeUrl("")     
				.facebookUrl("")   
				.twitterUrl("")     
				.linkedinUrl("")   
				.build();

		GMBOperationHours gmbOperationHours = gmbOperationHoursService.getGMBOperationHours(location);

		gmbLocation.setGmbOperationHours(gmbOperationHours);
		return gmbLocation;
	}

	public String createQRCode(String clientName, String fileName, String url)
			throws SQLException, UnirestException, IOException, InterruptedException {

		int WIDTH = 2160;
		int HEIGHT = 2160;
		String qrCodeImageName = "";

		if (url.isEmpty()) {
			return qrCodeImageName;
		}

		GMBSettings gmbSettings = gmbSettingsRepository.getByName(GMBSettings.QR_LOGO_IMAGE);

		if (gmbSettings != null
				&& (gmbSettings.getValue().equalsIgnoreCase("1") || gmbSettings.getValue().equalsIgnoreCase("true"))) {
			String logoImagePath = fileUploadDir + File.separator + clientName
					+ "_qr_logo.png";

			String outputFilePath = fileUploadDir + File.separator + "QR" + File.separator
					+ clientName + File.separator;

			Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix bitMatrix = null;
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			try {
				// cleanDirectory(DIR);
				// initDirectory(DIR);
				bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
				BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, getMatrixConfig());
				BufferedImage overly = ImageIO.read(new File(logoImagePath));
				int deltaHeight = qrImage.getHeight() - overly.getHeight();
				int deltaWidth = qrImage.getWidth() - overly.getWidth();
				BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(),
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = (Graphics2D) combined.getGraphics();
				g.drawImage(qrImage, 0, 0, null);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				g.drawImage(overly, (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2), null);
				ImageIO.write(combined, "png", os);
				qrCodeImageName = fileName.replaceAll(" ", "_") + ".png";
				Files.copy(new ByteArrayInputStream(os.toByteArray()), Paths.get(outputFilePath + qrCodeImageName),
						StandardCopyOption.REPLACE_EXISTING);

			} catch (WriterException e) {
				LOG.error("QR code generation failed for {}: {}", fileName, e.getMessage(), e);
				throw new RuntimeException("QR code generation failed for: " + fileName, e);
			} catch (IOException e) {
				LOG.error("QR code file write failed for {}: {}", fileName, e.getMessage(), e);
				throw new RuntimeException("QR code file write failed for: " + fileName, e);
			}
		} else {
			/*	url = Configuration.getServerPath() + "/web/pages/qr/qrTracker.jsp?client_name=" + clientName
					+ "&dealer_id=" + dealerId + "&review_url=" + url;*/
			url = URLEncoder.encode(url, Charset.forName("UTF-8"));
			url = "http://api.qrserver.com/v1/create-qr-code/?data=" + url;

			String outputFilePath = fileUploadDir + File.separator + "QR" + File.separator
					+ clientName + File.separator + fileName.replaceAll(" ", "_") + ".png";

			System.out.println("output path ::" + outputFilePath);

			OutputStream os = null;
			InputStream is = null;

			try {
				os = new FileOutputStream(outputFilePath);

				for (int i = 0; i < 3; i++) {
					try {
						is = Unirest.get(url).asBinary().getBody();
						byte[] buffer = new byte[8 * 1024];
						int bytesRead;
						while ((bytesRead = is.read(buffer)) != -1) {
							os.write(buffer, 0, bytesRead);
						}
						qrCodeImageName = fileName.replaceAll(" ", "_") + ".png";
						break; // success, exit loop
					} catch (UnirestException e) {
						Thread.sleep(1000); // delay before retry
						LOG.warn("QR code download attempt {} failed for {}: {}", i + 1, fileName, e.getMessage());
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (IOException ioe) {
								LOG.warn("Error closing QR code InputStream: {}", ioe.getMessage());
							}
						}
					}
				}
			} catch (Exception e) {
				LOG.error("Failed to download QR code image for {}: {}", fileName, e.getMessage(), e);
				throw new RuntimeException("Failed to download QR code image for: " + fileName, e);
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException ioe) {
						LOG.warn("Error closing QR code OutputStream: {}", ioe.getMessage());
					}
				}
			}
		}
		return qrCodeImageName;

	}
	private MatrixToImageConfig getMatrixConfig() {
		return new MatrixToImageConfig();
	}

	public enum Colors {

		BLUE(0xFF40BAD0), RED(0xFFE91C43), PURPLE(0xFF8A4F9E), ORANGE(0xFFF4B13D), WHITE(0xFFFFFFFF);
		// BLACK(0xFF000000);

		private final int argb;

		Colors(final int argb) {
			this.argb = argb;
		}

		public int getArgb() {
			return argb;
		}
	}

	public List<GMBAccount> fetchGMBAccount(String clientId) {
		List<GMBAccount> gmbAccounts = gmbAccountRepository.findByClientId(clientId);
		if(gmbAccounts.size()>0) {
			return gmbAccounts;
		}else {
			throw new InvalidRequestException("GMB Account is not availabe for given client id :: "+clientId);
		}
	}

	public GMBAccountLocationsResponse importGMBLocation(List<GMBAccount> gmbAccounts) {
//		if(gmbAccounts.size()>1) {
//			throw new InvalidRequestException("User can only Select one Account at a time. selected more than 1 "+gmbAccounts.size());
//		}else {
			GMBAccount selectedAccount = gmbAccounts.get(0);
			try {
				String clientId = selectedAccount.getClientId();

				List<String> accountIds = gmbAccounts.stream()
						.map(GMBAccount::getAccountId)
						.collect(Collectors.toList());

				gmbAccountRepository.updateSelectedAccountStatus(clientId, accountIds);

				List<GMBAccount> selectedAccounts = gmbAccountRepository.findByClientIdAndStatus(clientId, "selected");

				// Each account's DB lookup + Google fetch is independent, so fan them out
				// and join before returning - keeps this endpoint's synchronous contract
				// while overlapping the accounts' network latency instead of paying it serially.
				List<CompletableFuture<List<GMBLocation>>> futures = selectedAccounts.stream()
						.map(gmbAccount -> CompletableFuture.supplyAsync(
								() -> fetchNewLocationsForAccount(gmbAccount), gmbImportPreviewExecutor))
						.collect(Collectors.toList());

				// Keyed by account_name. Names aren't guaranteed unique per client, so if two accounts
				// share a name their location lists are merged under that key rather than one silently
				// overwriting the other.
				Map<String, List<GMBLocation>> gmbLocationsByAccount = new LinkedHashMap<>();
				for (int i = 0; i < selectedAccounts.size(); i++) {
					String accountName = selectedAccounts.get(i).getAccountName();
					List<GMBLocation> locations = futures.get(i).join();
					gmbLocationsByAccount.merge(accountName, locations, (existing, incoming) -> {
						List<GMBLocation> merged = new ArrayList<>(existing);
						merged.addAll(incoming);
						return merged;
					});
				}

				List<GMBAccountLocationsResponse.SelectedAccount> selectedAccountSummaries = selectedAccounts.stream()
						.map(gmbAccount -> GMBAccountLocationsResponse.SelectedAccount.builder()
								.accountId(gmbAccount.getAccountId())
								.accountName(gmbAccount.getAccountName())
								.build())
						.collect(Collectors.toList());

				return GMBAccountLocationsResponse.builder()
//						.accountId(selectedAccount.getAccountId())
//						.accountName(selectedAccount.getAccountName())
						.gmbLocations(gmbLocationsByAccount)
//						.selectedAccounts(selectedAccountSummaries)
						.build();

			} catch (Exception e) {
				e.printStackTrace();
				return GMBAccountLocationsResponse.builder()
//						.accountId(selectedAccount.getAccountId())
//						.accountName(selectedAccount.getAccountName())
						.gmbLocations(Collections.emptyMap())
//						.selectedAccounts(Collections.emptyList())
						.build();
			}
//		}
	}

	public List<GMBLocation> fetchNewLocationsForAccount(GMBAccount gmbAccount) {
		try {
			Set<String> existingLocationIds = new HashSet<>();
			List<GMBLocation> dbLocations = getAllGMBLocationsByAccountIdAndClientId(gmbAccount.getAccountId(), gmbAccount.getClientId());
			dbLocations.forEach(location ->
			existingLocationIds.add(location.getGmbLocationId()));

			// Locations from Google
			List<GMBLocation> consoleLocations = getLocations(gmbAccount, true);

			List<GMBLocation> newLocations = new ArrayList<>();
			for (GMBLocation location : consoleLocations) {
				if (!existingLocationIds.contains(location.getGmbLocationId())) {
					newLocations.add(location);
				}
			}
			return newLocations;

		} catch (Exception e) {
			LOG.error("Error importing GMB locations for account {}: {}", gmbAccount.getAccountId(), e.getMessage(), e);
			return Collections.emptyList();
		}
	}


	public void insertGMBLocation(GMBLocation gmbLoc) {
		createNewLocation(gmbLoc.getClientId(), gmbLoc);
		List<DealerLocation> dealerLocations = dealerLocationService.getAllDealerLocationsByClientId(gmbLoc.getClientId());
		Set<String> dealerlocationIds = dealerLocations.stream()
				.map(DealerLocation::getDealerId)
				.collect(Collectors.toSet());
		if (!dealerlocationIds.contains(gmbLoc.getDealerId())) {
			importDealerLocationFromGmb(gmbLoc.getClientId(), gmbLoc);
		}
		insertUserClientLocationMapping(gmbLoc.getClientId(), gmbLoc);
	}

	public void insertGMBLocation(List<GMBLocation> gmbLocList) {
		Plan plan = planRepository.findByClientId(gmbLocList.get(0).getClientId());
		if(plan == null) {
			throw new InvalidRequestException("Plan is not created yet for Client Id"+gmbLocList.get(0).getClientId());
		}
		if(gmbLocList.size() <= plan.getLocationCount()) {
			for (GMBLocation gmbLoc : gmbLocList) {
				try {
					insertGMBLocation(gmbLoc);
				} catch (Exception e) {
					log.error("Error inserting GMB location {}: {}", gmbLoc.getGmbLocationId(), e.getMessage(), e);
				}
			}
			onboardingService.markGmbLocationSelected(gmbLocList.get(0).getClientId());
			seedReviewAndInsightSettings(gmbLocList.get(0).getClientId());
		}else {
			throw new InvalidRequestException("Selected more Location than planed location count : " + gmbLocList.size() + " Actual planned count is : "+plan.getLocationCount());
		}
	}

	private void seedReviewAndInsightSettings(String clientId) {
		if (reviewSettingsRepository.findByClientIdAndPlatform(clientId, ReviewSettings.PLATFORM_GMB) == null) {
			ReviewSettings settings = new ReviewSettings();
			settings.setClientId(clientId);
			settings.setPlatform(ReviewSettings.PLATFORM_GMB);
			settings.setReviewLastInsertedDate("2000-01-01T00:00:00Z");
			settings.setAutoResponseDate(SEED_REVIEW_AUTO_RESPONSE_DATE);
			reviewSettingsRepository.save(settings);
		}

		Set<String> existingDealerIds = gmbInsightService.findByClientId(clientId).stream()
				.map(GMBInsightSettings::getDealerId)
				.collect(Collectors.toSet());

		Set<String> dealerIds = gmbLocationRepository.findByClientId(clientId).stream()
				.map(GMBLocation::getDealerId)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toSet());

		for (String dealerId : dealerIds) {
			if (!existingDealerIds.contains(dealerId)) {
				gmbInsightService.saveGMBInsightSettings(clientId, dealerId, SEED_INSIGHT_LAST_INSERTED_DATE);
			}
		}
	}

	@Async("clientTaskExecutor")
	public void triggerReviewDeploymentAsync(String clientId) {
		try {
			String xml = "<params>"
					+ "<client-id>" + clientId + "</client-id>"
					+ "<fetch-reviews-account>true</fetch-reviews-account>"
					+ "<process-review>true</process-review>"
					+ "<process-auto-response>true</process-auto-response>"
					+ "</params>";

			String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
			String logFilePath = logPath + "adhoc_GMBReviewDeployementTask_" + clientId + "_" + timestamp + ".log";

			gmbReviewDeployementTask.setParameters(xml);
			gmbReviewDeployementTask.setLogFile(logFilePath);
			gmbReviewDeployementTask.run();
		} catch (Exception e) {
			log.error("Failed to trigger GMBReviewDeployementTask for client {}: {}", clientId, e.getMessage(), e);
		}
	}

	@Async("clientTaskExecutor")
	public void triggerInsightDeploymentAsync(String clientId) {
		try {
			String xml = "<params>"
					+ "<client-id>" + clientId + "</client-id>"
					+ "<fetch-gmb-insight>true</fetch-gmb-insight>"
					+ "<insert-SQL>true</insert-SQL>"
					+ "</params>";

			String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
			String logFilePath = logPath + "adhoc_GMBInsightDeploymentTask_" + clientId + "_" + timestamp + ".log";

			gmbInsightDeploymentTask.setParameters(xml);
			gmbInsightDeploymentTask.setLogFile(logFilePath);
			gmbInsightDeploymentTask.run();
		} catch (Exception e) {
			log.error("Failed to trigger GMBInsightDeploymentTask for client {}: {}", clientId, e.getMessage(), e);
		}
	}

	@Async("clientTaskExecutor")
	public void triggerMediaFetchAsync(String clientId) {
		try {
			String xml = "<params>"
					+ "<client-id>" + clientId + "</client-id>"
					+ "<fetch-gmb-media>true</fetch-gmb-media>"
					+ "</params>";

			String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
			String logFilePath = logPath + "adhoc_GMBMediaFetchTask_" + clientId + "_" + timestamp + ".log";

			gmbMediaFetchTask.setParameters(xml);
			gmbMediaFetchTask.setLogFile(logFilePath);
			gmbMediaFetchTask.run();
		} catch (Exception e) {
			log.error("Failed to trigger GMBMediaFetchTask for client {}: {}", clientId, e.getMessage(), e);
		}
	}

	@Async("clientTaskExecutor")
	public void processFetchLocations(String clientId, GMBAccount gmbAccount, boolean fetchLocationStatus, boolean importGMBLocations, boolean importDealerLocation,
			List<GMBLocation> gmbAllLocation, Map<String, GMBLocation> gmbLocationIdVsDbLocationMap, Map<String, GMBLocation> gmbDealerIdVsDbLocationMap,
			Set<String> operationlocationIds, Set<String> dealerlocationIds, Integer maxLocationsToProcess) throws Exception {

		log.info("API Call");
		List<GMBLocation> gmbLocationConsole = getLocations(gmbAccount, fetchLocationStatus);
		log.info("API Call Completed");

		gmbAllLocation.addAll(gmbLocationConsole);

		for (GMBLocation gmbLoc : gmbAllLocation) {

			log.info("Processing Location Id - "+gmbLoc.getGmbLocationId());
			boolean containsLocationId = gmbLocationIdVsDbLocationMap.containsKey(gmbLoc.getGmbLocationId());
			boolean containsDealerId = gmbDealerIdVsDbLocationMap.containsKey(gmbLoc.getDealerId());

			/*	long gmbId = containsLocationId ? gmbLocationIdVsDbLocationMap.get(gmbLoc.getGmbLocationId())
					: containsDealerid ? gmbDealerIdVsDbLocationMap.get(gmbLoc.getDealerId()) : 0L;*/

			String gmbLocationId = containsLocationId ? gmbLoc.getGmbLocationId() : containsDealerId && gmbDealerIdVsDbLocationMap.get(gmbLoc.getDealerId()) != null
					? gmbDealerIdVsDbLocationMap.get(gmbLoc.getDealerId()).getGmbLocationId() : "";
			try {

				if(containsLocationId || containsDealerId) {
					if (fetchLocationStatus) {

						log.info("Updating status for GMB Location Id: {} | Status: {}", gmbLocationId, gmbLoc.getStatus());
						gmbLocationRepository.updateStatusByClientIdAndGmbLocationId(gmbLoc.getStatus(), clientId, gmbLocationId);
					}
					GMBLocation location = gmbLocationIdVsDbLocationMap.get(gmbLoc.getGmbLocationId());
					if(location == null) {
						location = gmbDealerIdVsDbLocationMap.get(gmbLoc.getDealerId());
					}
					log.info("Updating GMB Location table for GMB Location Id: {}", gmbLocationId);

					updateExistingLocation(location, gmbLoc, clientId, operationlocationIds);

				}  
				else if (importGMBLocations) {
					log.info("Importing new GMB Location: {}", gmbLoc.getGmbLocationId());

					createNewLocation(clientId, gmbLoc);

					insertUserClientLocationMapping(clientId, gmbLoc);

					//	gmbInsightService.insertGMBInsightSettings(clientName, gmbLoc.getDealerId(), lastProcessingDate, GMBInsightSettings.TYPE_INSIGHTS);
				}  
				if (importDealerLocation && !dealerlocationIds.contains(gmbLoc.getDealerId())) {

					importDealerLocationFromGmb(clientId, gmbLoc);
				}
				GMBLocation gmbLocationDb = containsLocationId ? gmbLocationIdVsDbLocationMap.get(gmbLoc.getGmbLocationId())
						: containsDealerId ? gmbDealerIdVsDbLocationMap.get(gmbLoc.getDealerId()) : null;

				DealerLocation dealerLocation = dealerLocationRepository.getDealerLocationByDealerIdAndClientId(gmbLoc.getDealerId(), clientId);
				/*
			if (gmbLocationDb != null) {

				if (!gmbLocationDb.getNewReviewUrl().equalsIgnoreCase(gmbLoc.getNewReviewUrl())
						|| !dealerLocation.isQrCreated()) {
					/*	String qrImage = gmbLocationService.createQRCode(clientName, gmbLoc.getDealerId(),
							gmbLoc.getNewReviewUrl());*/
				/*	String url = serverPath + "/web/pages/qr/qrTracker.jsp?client_name=" + clientId
							+ "&dealer_id=" + gmbLoc.getDealerId() + "&review_url=" + gmbLoc.getNewReviewUrl();
					String qrImage = createQRCode(clientId, gmbLoc.getDealerId(), url);
					dealerLocationRepository.updateQrInfoByDealerId(true, qrImage, gmbLoc.getDealerId(), clientId);
				}
			}*/
			}catch (Exception e) {
				log.error("Error processing GMB Location ID {}: {}", gmbLoc.getGmbLocationId(), e.getMessage(), e);
			}

		}
		//	List<GMBLocation> gmbLocationDB = getAllGmbLocationByClientId(clientId);
		/*	if (insertBQ) {
			gmbLocationService.deleteGMBLocationBQ(bigQuery, clientName);
			gmbLocationService.insertGMBLocationBQ(bigQuery, clientName, gmbLocationDB);
		}*/

	}

	/**
	 * Blocking counterpart to {@link #processFetchLocations}, for callers (e.g. the OAuth
	 * callback) that need the fetch to complete before proceeding. Builds the same lookup
	 * maps/sets that {@link com.caliper.task.GMBDeploymentTask} builds per account, then runs
	 * the same fetch/process loop synchronously on the calling thread.
	 */
	public void fetchAndProcessLocationsSync(String clientId, GMBAccount gmbAccount, boolean fetchLocationStatus,
			boolean importGMBLocations, boolean importDealerLocation) throws Exception {

		List<GMBLocation> dbGmbLocations = getAllGMBLocationsByAccountId(gmbAccount.getAccountId());
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

		List<GMBLocation> gmbAllLocation = new ArrayList<>();

		log.info("API Call");
		List<GMBLocation> gmbLocationConsole = getLocations(gmbAccount, fetchLocationStatus);
		log.info("API Call Completed");

		gmbAllLocation.addAll(gmbLocationConsole);

		for (GMBLocation gmbLoc : gmbAllLocation) {

			log.info("Processing Location Id - "+gmbLoc.getGmbLocationId());
			boolean containsLocationId = gmbLocationIdVsDbLocationMap.containsKey(gmbLoc.getGmbLocationId());
			boolean containsDealerId = gmbDealerIdVsDbLocationMap.containsKey(gmbLoc.getDealerId());

			String gmbLocationId = containsLocationId ? gmbLoc.getGmbLocationId() : containsDealerId && gmbDealerIdVsDbLocationMap.get(gmbLoc.getDealerId()) != null
					? gmbDealerIdVsDbLocationMap.get(gmbLoc.getDealerId()).getGmbLocationId() : "";
			try {

				if(containsLocationId || containsDealerId) {
					if (fetchLocationStatus) {

						log.info("Updating status for GMB Location Id: {} | Status: {}", gmbLocationId, gmbLoc.getStatus());
						gmbLocationRepository.updateStatusByClientIdAndGmbLocationId(gmbLoc.getStatus(), clientId, gmbLocationId);
					}
					GMBLocation location = gmbLocationIdVsDbLocationMap.get(gmbLoc.getGmbLocationId());
					if(location == null) {
						location = gmbDealerIdVsDbLocationMap.get(gmbLoc.getDealerId());
					}
					log.info("Updating GMB Location table for GMB Location Id: {}", gmbLocationId);

					updateExistingLocation(location, gmbLoc, clientId, operationlocationIds);

				}
				else if (importGMBLocations) {
					log.info("Importing new GMB Location: {}", gmbLoc.getGmbLocationId());

					createNewLocation(clientId, gmbLoc);

					insertUserClientLocationMapping(clientId, gmbLoc);
				}
				if (importDealerLocation && !dealerlocationIds.contains(gmbLoc.getDealerId())) {

					importDealerLocationFromGmb(clientId, gmbLoc);
				}
				GMBLocation gmbLocationDb = containsLocationId ? gmbLocationIdVsDbLocationMap.get(gmbLoc.getGmbLocationId())
						: containsDealerId ? gmbDealerIdVsDbLocationMap.get(gmbLoc.getDealerId()) : null;

				DealerLocation dealerLocation = dealerLocationRepository.getDealerLocationByDealerIdAndClientId(gmbLoc.getDealerId(), clientId);
			}catch (Exception e) {
				log.error("Error processing GMB Location ID {}: {}", gmbLoc.getGmbLocationId(), e.getMessage(), e);
			}

		}
	}

	private void updateExistingLocation(GMBLocation location, GMBLocation gmbLoc, String clientId, Set<String>operationlocationIds) {
		GMBOperationHours gmbHours = gmbLoc.getGmbOperationHours();

		System.out.println("gmbLoc.getGmbLocationId() - "+gmbLoc.getGmbLocationId());

		System.out.println("location.getGmbLocationId() - "+location.getGmbLocationId());


		if (operationlocationIds.contains(gmbLoc.getGmbLocationId())) {

			// UPDATE
			GMBOperationHours hours =
					gmbOperationHoursRepository.findByClientIdAndGmbLocationId(clientId, gmbLoc.getGmbLocationId());

			if (hours != null && gmbHours != null) {
				hours.setMondayOpenTime(gmbHours.getMondayOpenTime());
				hours.setMondayCloseTime(gmbHours.getMondayCloseTime());
				hours.setTuesdayOpenTime(gmbHours.getTuesdayOpenTime());
				hours.setTuesdayCloseTime(gmbHours.getTuesdayCloseTime());
				hours.setWednesdayOpenTime(gmbHours.getWednesdayOpenTime());
				hours.setWednesdayCloseTime(gmbHours.getWednesdayCloseTime());
				hours.setThursdayOpenTime(gmbHours.getThursdayOpenTime());
				hours.setThursdayCloseTime(gmbHours.getThursdayCloseTime());
				hours.setFridayOpenTime(gmbHours.getFridayOpenTime());
				hours.setFridayCloseTime(gmbHours.getFridayCloseTime());
				hours.setSaturdayOpenTime(gmbHours.getSaturdayOpenTime());
				hours.setSaturdayCloseTime(gmbHours.getSaturdayCloseTime());
				hours.setSundayOpenTime(gmbHours.getSundayOpenTime());
				hours.setSundayCloseTime(gmbHours.getSundayCloseTime());

				gmbOperationHoursRepository.save(hours);
				location.setGmbOperationHours(hours);
			}

		} else {

			// CREATE
			if (gmbHours != null) {
				GMBOperationHours hours = GMBOperationHours.builder()
						.clientId(clientId)
						.dealerId(gmbLoc.getDealerId())
						.gmbLocationId(gmbLoc.getGmbLocationId())
						.mondayOpenTime(gmbHours.getMondayOpenTime())
						.mondayCloseTime(gmbHours.getMondayCloseTime())
						.tuesdayOpenTime(gmbHours.getTuesdayOpenTime())
						.tuesdayCloseTime(gmbHours.getTuesdayCloseTime())
						.wednesdayOpenTime(gmbHours.getWednesdayOpenTime())
						.wednesdayCloseTime(gmbHours.getWednesdayCloseTime())
						.thursdayOpenTime(gmbHours.getThursdayOpenTime())
						.thursdayCloseTime(gmbHours.getThursdayCloseTime())
						.fridayOpenTime(gmbHours.getFridayOpenTime())
						.fridayCloseTime(gmbHours.getFridayCloseTime())
						.saturdayOpenTime(gmbHours.getSaturdayOpenTime())
						.saturdayCloseTime(gmbHours.getSaturdayCloseTime())
						.sundayOpenTime(gmbHours.getSundayOpenTime())
						.sundayCloseTime(gmbHours.getSundayCloseTime())
						.build();

				gmbOperationHoursRepository.save(hours);
				location.setGmbOperationHours(hours);
			}
		}
	}

	private void createNewLocation(String clientId, GMBLocation gmbLoc) {

		GMBLocation location = GMBLocation.builder()
				.gmbLocationId(gmbLoc.getGmbLocationId().trim())
				.clientId(clientId)
				.accountId(gmbLoc.getAccountId())
				.dealerId(gmbLoc.getDealerId())
				.name(gmbLoc.getName())
				.address(gmbLoc.getAddress())
				.address1(gmbLoc.getAddress1())
				.address2(gmbLoc.getAddress2())
				.address3(gmbLoc.getAddress3())
				.area(gmbLoc.getArea())
				.city(gmbLoc.getCity())
				.state(StringUtils.isEmpty(gmbLoc.getState()) ? gmbLoc.getLabels() : gmbLoc.getState())
				.countryCode(gmbLoc.getCountryCode())
				.pincode(gmbLoc.getPincode())
				.latitude(gmbLoc.getLatitude())
				.longitude(gmbLoc.getLongitude())
				.phoneNumber(gmbLoc.getPhoneNumber())
				.additionalPhones(gmbLoc.getAdditionalPhones())
				.mapUrl(gmbLoc.getMapUrl())
				.newReviewUrl(gmbLoc.getNewReviewUrl())
				.openInfoCanReopen(gmbLoc.getOpenInfoCanReopen())
				.openInfoStatus(gmbLoc.getOpenInfoStatus())
				.websiteUrl(gmbLoc.getWebsiteUrl())
				.description(gmbLoc.getDescription())
				.primaryCategory(gmbLoc.getPrimaryCategory())
				.additionalCategories(gmbLoc.getAdditionalCategories())
				.labels(gmbLoc.getLabels())
				.status(gmbLoc.getStatus())
				.insertedDate(new Date())
				.appointmentLink(gmbLoc.getAppointmentLink())
				.placeActionId(gmbLoc.getPlaceActionId())
				.languageCode(gmbLoc.getLanguageCode())
				.status(gmbLoc.getStatus())
				.build();

		GMBOperationHours existingHours = location.getGmbOperationHours();

		if (existingHours != null) {

			existingHours.setMondayOpenTime(gmbLoc.getGmbOperationHours().getMondayOpenTime());
			existingHours.setMondayCloseTime(gmbLoc.getGmbOperationHours().getMondayCloseTime());
			existingHours.setTuesdayOpenTime(gmbLoc.getGmbOperationHours().getTuesdayOpenTime());
			existingHours.setTuesdayCloseTime(gmbLoc.getGmbOperationHours().getTuesdayCloseTime());
			existingHours.setWednesdayOpenTime(gmbLoc.getGmbOperationHours().getWednesdayOpenTime());
			existingHours.setWednesdayCloseTime(gmbLoc.getGmbOperationHours().getWednesdayCloseTime());
			existingHours.setThursdayOpenTime(gmbLoc.getGmbOperationHours().getThursdayOpenTime());
			existingHours.setThursdayCloseTime(gmbLoc.getGmbOperationHours().getThursdayCloseTime());
			existingHours.setFridayOpenTime(gmbLoc.getGmbOperationHours().getFridayOpenTime());
			existingHours.setFridayCloseTime(gmbLoc.getGmbOperationHours().getFridayCloseTime());
			existingHours.setSaturdayOpenTime(gmbLoc.getGmbOperationHours().getSaturdayOpenTime());
			existingHours.setSaturdayCloseTime(gmbLoc.getGmbOperationHours().getSaturdayCloseTime());
			existingHours.setSundayOpenTime(gmbLoc.getGmbOperationHours().getSundayOpenTime());
			existingHours.setSundayCloseTime(gmbLoc.getGmbOperationHours().getSundayCloseTime());

		} else {

			GMBOperationHours operationHours = GMBOperationHours.builder()
					.clientId(clientId)
					.dealerId(gmbLoc.getDealerId())
					.gmbLocationId(gmbLoc.getGmbLocationId())
					.mondayOpenTime(gmbLoc.getGmbOperationHours().getMondayOpenTime())
					.mondayCloseTime(gmbLoc.getGmbOperationHours().getMondayCloseTime())
					.tuesdayOpenTime(gmbLoc.getGmbOperationHours().getTuesdayOpenTime())
					.tuesdayCloseTime(gmbLoc.getGmbOperationHours().getTuesdayCloseTime())
					.wednesdayOpenTime(gmbLoc.getGmbOperationHours().getWednesdayOpenTime())
					.wednesdayCloseTime(gmbLoc.getGmbOperationHours().getWednesdayCloseTime())
					.thursdayOpenTime(gmbLoc.getGmbOperationHours().getThursdayOpenTime())
					.thursdayCloseTime(gmbLoc.getGmbOperationHours().getThursdayCloseTime())
					.fridayOpenTime(gmbLoc.getGmbOperationHours().getFridayOpenTime())
					.fridayCloseTime(gmbLoc.getGmbOperationHours().getFridayCloseTime())
					.saturdayOpenTime(gmbLoc.getGmbOperationHours().getSaturdayOpenTime())
					.saturdayCloseTime(gmbLoc.getGmbOperationHours().getSaturdayCloseTime())
					.sundayOpenTime(gmbLoc.getGmbOperationHours().getSundayOpenTime())
					.sundayCloseTime(gmbLoc.getGmbOperationHours().getSundayCloseTime())
					.build();

			location.setGmbOperationHours(operationHours);
		}
		gmbLocationRepository.save(location);
	}

	private void insertUserClientLocationMapping(String clientId, GMBLocation gmbLocation) {
		Client client = clientRepository.findByClientId(clientId)
				.orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));

		UserClientLocMapping clientLocMapping = new UserClientLocMapping();
		clientLocMapping.setClientId(clientId);
		clientLocMapping.setDealerId(gmbLocation.getDealerId());
		clientLocMapping.setUserId(client.getEmail());

		userClientLocMappingRepository.save(clientLocMapping);
	}

	private void importDealerLocationFromGmb(String clientId, GMBLocation gmbLoc) {

		GMBOperationHours operationHours = gmbLoc.getGmbOperationHours();
		//what if dealer obj already present in table?
		DealerLocation dealerLocation = new DealerLocation();

		dealerLocation.setClientId(clientId);
		dealerLocation.setGmbLocationId(gmbLoc.getGmbLocationId());
		dealerLocation.setNameWithLocationDesc(gmbLoc.getName());
		dealerLocation.setDealerId(gmbLoc.getDealerId());
		dealerLocation.setDealerName(gmbLoc.getName());

		dealerLocation.setAddress(gmbLoc.getAddress());
		dealerLocation.setAddress1(gmbLoc.getAddress1());
		dealerLocation.setAddress2(gmbLoc.getAddress2());
		dealerLocation.setAddress3(gmbLoc.getAddress3());
		dealerLocation.setArea(gmbLoc.getArea());
		dealerLocation.setCircle(gmbLoc.getLabels());

		dealerLocation.setCity(gmbLoc.getCity());
		dealerLocation.setState(gmbLoc.getState());
		dealerLocation.setCountryCode(gmbLoc.getCountryCode());
		dealerLocation.setCountry(GMBHelper.getCountryByCode(gmbLoc.getCountryCode()));
		dealerLocation.setPincode(String.valueOf(gmbLoc.getPincode()));

		dealerLocation.setLatitude(gmbLoc.getLatitude());
		dealerLocation.setLongitude(gmbLoc.getLongitude());

		dealerLocation.setGmbPhoneNumber(gmbLoc.getPhoneNumber());
		//	dealerLocation.setEmail(gmbLoc.getEmail());
		dealerLocation.setStorePageUrl(gmbLoc.getWebsiteUrl());
		dealerLocation.setDescription(gmbLoc.getDescription());

		dealerLocation.setPrimaryCategoryGMB(gmbLoc.getPrimaryCategory());
		dealerLocation.setAdditionalCategoriesGMB(gmbLoc.getAdditionalCategories());

		dealerLocation.setOpenInfoCanReopen(gmbLoc.getOpenInfoCanReopen());
		dealerLocation.setOpenInfoStatus(gmbLoc.getOpenInfoStatus());

		dealerLocation.setAdditionalPhones(gmbLoc.getAdditionalPhones());
		dealerLocation.setStatus(gmbLoc.getStatus());
		dealerLocation.setAppointmentLink(gmbLoc.getAppointmentLink());

		dealerLocation.setLanguageCode(gmbLoc.getLanguageCode());

		dealerLocation.setGmbEnabled(true);
		dealerLocation.setFacebookEnabled(false);
		dealerLocation.setStorePageEnabled(false);

		dealerLocation.setFacebookPageId("");
		dealerLocation.setInsertedDate(new Date());

		dealerLocation.setQrCreated(false);
		dealerLocation.setQrImage("");

		dealerLocationService.saveLocation(dealerLocation);

		String dealerId = gmbLoc.getDealerId();
		// what if op hr object already present in dealer op hr table
		dealerLocationService.insertDealerOperationHours(clientId, dealerId, operationHours, dealerLocation);

	}

	public Long fetchLocationsCount(String clientId, Set<String> dealerIds) {
		return gmbLocationRepository.countByClientIdAndDealerIdIn(clientId, dealerIds);
	}

	public List<GMBLocation> fetchLocationsByClientAndDealerIds(String clientId, Set<String> dealerIds) {
		return gmbLocationRepository.findByClientIdAndDealerIdIn(clientId, new ArrayList<>(dealerIds));
	}


	public List<GMBLocation> getFilteredGMBLocation(LocationFilterRequest req) {

		List<UserClientLocMapping> userClientLocMapping =
				userClientLocMappingRepository.findByUserIdAndclientId(
						req.getUserId(), req.getClientId());

		Set<String> dealerIds = userClientLocMapping.stream()
				.map(UserClientLocMapping::getDealerId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		if (dealerIds.isEmpty()) {
			throw new ResourceNotFoundException(
					"Dealers are not mapped for given user id : " + req.getUserId()+ " and client id : " + req.getClientId());
		}

		List<String> validDealerIds;

		// Request dealerId is null or empty. allow all user dealers
		if (req.getDealerId() == null || req.getDealerId().isEmpty()) {
			validDealerIds = new ArrayList<>(dealerIds);
		}
		// Request dealerId present 
		else {
			validDealerIds = req.getDealerId().stream()
					.filter(dealerIds::contains)
					.toList();

			if (validDealerIds.isEmpty()) {
				return Collections.emptyList();
			}
		}

		List<GMBLocation> locations = gmbLocationRepository.findByClientIdAndDealerIdIn(req.getClientId(), validDealerIds);
		return locations;
	}

}

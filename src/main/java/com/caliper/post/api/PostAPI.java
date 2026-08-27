package com.caliper.post.api;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.location.facebook.entity.FacebookLocation;
import com.caliper.location.facebook.repository.FacebookLocationRepository;
import com.caliper.location.facebook.service.FacebookLocationService;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.repository.GMBLocationRepository;
import com.caliper.location.gmb.service.GMBLocationService;
import com.caliper.post.dto.Response.FacebookPostResponse;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.caliper.post.entity.Post;
import com.caliper.post.repository.PostRepository;
import com.google.api.services.mybusiness.v4.MyBusiness;
import com.google.api.services.mybusiness.v4.model.CallToAction;
import com.google.api.services.mybusiness.v4.model.LocalPost;
import com.google.api.services.mybusiness.v4.model.LocalPostEvent;
import com.google.api.services.mybusiness.v4.model.LocalPostOffer;
import com.google.api.services.mybusiness.v4.model.MediaItem;
import com.google.api.services.mybusiness.v4.model.TimeInterval;
import com.google.api.services.mybusiness.v4.model.TimeOfDay;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.FacebookType;
import com.restfb.types.PostCallToAction;
import com.restfb.types.PostCallToAction.PostCallToActionValue;
import com.google.api.services.mybusiness.v4.model.LocalPost;

@Component
public class PostAPI {
	
	@Autowired
	private GMBLocationService gmbLocationService;
	
	@Autowired
	private FacebookLocationService facebookLocationService;
	
	
	//--------------------------------------------------------------GMB POST API-------------------------------------------------------------

	public String createGMBPostUpdated(String clientId, MyBusiness business, Post post, String dealerId) throws SQLException, IOException {
		LocalPost localPost = new LocalPost();
		LocalPostEvent event = new LocalPostEvent();
		TimeInterval timeInterval = new TimeInterval();
		TimeOfDay startTime = new TimeOfDay();
		TimeOfDay endTime = new TimeOfDay();
		com.google.api.services.mybusiness.v4.model.Date startDate = new com.google.api.services.mybusiness.v4.model.Date();
		com.google.api.services.mybusiness.v4.model.Date endDate = new com.google.api.services.mybusiness.v4.model.Date();

		GMBLocation gmbLoc = gmbLocationService.getAllGmbLocationByClientIdAndDealerId(clientId, dealerId);
		if (gmbLoc == null) {
			throw new ResourceNotFoundException("GMB location not found for client: " + clientId + ", dealer: " + dealerId);
		}
		String locName = gmbLoc.getGmbLocationId();

		event.setTitle(post.getOfferTitle());
		//localPost.setLanguageCode("en-US");

		Calendar calStartDate = Calendar.getInstance();
		Calendar calEndDate = Calendar.getInstance();
		
		
		calStartDate.setTime(post.getStartDate());
		calEndDate.setTime(post.getEndDate());

		//START DATE AND START TIME
		startDate.setDay(calStartDate.get(Calendar.DATE));
		startDate.setMonth(calStartDate.get(Calendar.MONTH)+1);
		startDate.setYear(calStartDate.get(Calendar.YEAR));

		startTime.setHours(calStartDate.get(Calendar.HOUR_OF_DAY));
		startTime.setMinutes(calStartDate.get(Calendar.MINUTE));
		startTime.setSeconds(calStartDate.get(Calendar.SECOND));
		startTime.setNanos(0);
		System.out.println("post.getStartDate:: " + post.getStartDate());
		System.out.println("Start date :: "+startDate+" Start time: " + startTime);

		//END DATE AND END TIME
		endDate.setDay(calEndDate.get(Calendar.DATE));
		endDate.setMonth(calEndDate.get(Calendar.MONTH)+1);
		endDate.setYear(calEndDate.get(Calendar.YEAR));
		
		endTime.setHours(calEndDate.get(Calendar.HOUR_OF_DAY));  // Get hour (24-hour format)
		endTime.setMinutes(calEndDate.get(Calendar.MINUTE));    // Get minute
		endTime.setSeconds(calEndDate.get(Calendar.SECOND));    // Get second
		endTime.setNanos(0);
		System.out.println("post.getEndDate:: " + post.getEndDate());
		System.out.println("End date :: "+endDate+" End time: " + endTime);
		
		timeInterval.setStartDate(startDate);
		timeInterval.setEndDate(endDate);
		timeInterval.setStartTime(startTime);
		timeInterval.setEndTime(endTime);


		event.setSchedule(timeInterval);

		localPost.setEvent(event);

		if(post.getSummary() != null) {
			localPost.setSummary(post.getSummary());
		}

		MediaItem uploadURLMedia = new MediaItem();
		if(post.getMediaFormat().equalsIgnoreCase(Post.MEDIA_TYPE_PHOTO)) {
			uploadURLMedia.setMediaFormat(Post.MEDIA_TYPE_PHOTO);
		}else {
			uploadURLMedia.setMediaFormat(Post.MEDIA_TYPE_VIDEO);
		}
		//uploadURLMedia.setSourceUrl("http://fb1.interactiveavenues.net:8080/web/pages/icons/amaze.jpg");
		String imagePath = post.getImageUrl();
		String imagePathStr[] = imagePath.split(",");
		if(imagePathStr.length > 1) {
			// carousel
			List<MediaItem> mediaItemList = new ArrayList<>();

			for(String image : imagePathStr) {
				MediaItem uploadMultipleURLMedia = new MediaItem();
				uploadMultipleURLMedia.setSourceUrl(image);
				mediaItemList.add(uploadMultipleURLMedia);
				if(post.getMediaFormat().equalsIgnoreCase(Post.MEDIA_TYPE_PHOTO)) {
					uploadMultipleURLMedia.setMediaFormat(Post.MEDIA_TYPE_PHOTO);
				}else {
					uploadMultipleURLMedia.setMediaFormat(Post.MEDIA_TYPE_VIDEO);
				}
			}
			localPost.setMedia(new ArrayList<MediaItem>(mediaItemList));
		}else {
			uploadURLMedia.setSourceUrl(imagePath);
			localPost.setMedia(new ArrayList<MediaItem>(Arrays.asList(uploadURLMedia)));
		}
		if(post.getPostType().equalsIgnoreCase(Post.POST_TYPE_OFFER)) {
			LocalPostOffer offer = new LocalPostOffer();
			localPost.setTopicType("OFFER");

			if(post.getCouponCode() != null && post.getCouponCode() != "") {
				offer.setCouponCode(post.getCouponCode());
			}
			if(post.getRedeemUrl() != null && post.getRedeemUrl() != "") {
				offer.setRedeemOnlineUrl(post.getRedeemUrl());
			}
			if(post.getTermsConditions() != null && post.getTermsConditions()!= "") {
				offer.setTermsConditions(post.getTermsConditions());
			}
			localPost.setOffer(offer);
		}else {
			localPost.setTopicType("EVENT");
			CallToAction cta = new CallToAction();
			cta.setActionType(post.getActionType());

			String actionUrl = post.getActionUrl();
			if(actionUrl.contains("{")) {

				actionUrl = templatize(gmbLoc, post.getActionUrl());
			}
			System.out.println("actionUrl : "+actionUrl);
			cta.setUrl(actionUrl);

			localPost.setCallToAction(cta);
		}
		System.out.println("Local Post = "+localPost);		
		LocalPost lp = business.accounts().locations().localPosts().create(locName, localPost).execute();

		return lp.getName();

	}
	
	
	public String createGMBWhatsNewPostUpdated(String clientId, MyBusiness business, Post post, String dealerId)throws FileNotFoundException, IOException, SQLException {
		LocalPost localPost = new LocalPost();

		//		long locationId = post.getLocationId();
		GMBLocation gmbLoc = gmbLocationService.getAllGmbLocationByClientIdAndDealerId(clientId, dealerId);
		if (gmbLoc == null) {
			throw new ResourceNotFoundException("GMB location not found for client: " + clientId + ", dealer: " + dealerId);
		}
		String locName = gmbLoc.getGmbLocationId();

		localPost.setTopicType("STANDARD");
		localPost.setSummary(post.getSummary());

		CallToAction callToAction = new CallToAction();
		if(!post.getActionType().equalsIgnoreCase(Post.ACTION_TYPE_UNSPECIFIED)){
			if(post.getActionType().equalsIgnoreCase(Post.ACTION_TYPE_CALL)) {
				callToAction.setActionType(Post.ACTION_TYPE_CALL);
			}else {
				callToAction.setActionType(post.getActionType());
				String actionUrl = post.getActionUrl();
				if(actionUrl.contains("{")) {

					actionUrl = templatize(gmbLoc, post.getActionUrl());
				}
				System.out.println("actionUrl : "+actionUrl);
				callToAction.setUrl(actionUrl);
			}

			localPost.setCallToAction(callToAction);
		}
		MediaItem uploadURLMedia = new MediaItem();
		if(post.getMediaFormat().equalsIgnoreCase(Post.MEDIA_TYPE_PHOTO)) {
			uploadURLMedia.setMediaFormat(Post.MEDIA_TYPE_PHOTO);
		}else {
			uploadURLMedia.setMediaFormat(Post.MEDIA_TYPE_VIDEO);
		}
		uploadURLMedia.setSourceUrl(post.getImageUrl());

		localPost.setMedia(new ArrayList<MediaItem>(Arrays.asList(uploadURLMedia)));

		LocalPost lp = business.accounts().locations().localPosts().create(locName, localPost).execute();

		return lp.getName();
	}
	
	
	
	
	//--------------------------------------------------------------FACEBOOK POST API-------------------------------------------------------------
	
	public String uploadFacebookLinkPost(String clientId, Post post, String dealerId) throws SQLException {
		FacebookLocation facebookLocation = facebookLocationService.getFacebookLocationByClientIdAndDealerId(clientId, dealerId);
		FacebookClient fbClientChild = new DefaultFacebookClient(facebookLocation.getAccessToken(), Version.LATEST);
		PostCallToAction callToAction = new PostCallToAction() ;
		PostCallToActionValue postCallToActionValue = new PostCallToActionValue();
		callToAction.setType(post.getActionType());
		postCallToActionValue.setLinkTitle(post.getOfferTitle());

		GMBLocation gmblocation = gmbLocationService.getAllGmbLocationByClientIdAndDealerId(clientId, dealerId);

		String actionUrl = post.getActionUrl();
		if(actionUrl.contains("{")) {

			actionUrl = templatize(gmblocation, actionUrl);
		}
		postCallToActionValue.setLink(actionUrl);
		callToAction.setValue(postCallToActionValue);
		FacebookType response = fbClientChild.publish(facebookLocation.getFacebookPageId()+"/feed", FacebookType.class, 
				Parameter.with("message", post.getSummary()),
				Parameter.with("link", post.getActionUrl()),
				Parameter.with("call_to_action", callToAction)
				);
		System.out.println("fb.com/"+response.toString());

		return response.getId();
	}
	
	public String uploadFacebookMediaPost(String clientId, Post post,String dealerId) throws SQLException, FileNotFoundException {
		FacebookType response = new FacebookType();
		FacebookLocation facebookLocation = facebookLocationService.getFacebookLocationByClientIdAndDealerId(clientId, dealerId);
		FacebookClient fbClientChild = new DefaultFacebookClient(facebookLocation.getAccessToken(), Version.LATEST);
		String locationName = facebookLocation.getFacebookPageId();

		if(post.getMediaFormat().equalsIgnoreCase(Post.MEDIA_TYPE_PHOTO)) {
			FileInputStream fis = new FileInputStream(new File(post.getImageUrl()));
			response = fbClientChild.publish(locationName + "/photos", 
					FacebookType.class, BinaryAttachment.with("imageFile", fis),
					Parameter.with("caption", post.getSummary()));

		}
		else {
			//			FileInputStream fis = new FileInputStream(new File(post.getImagePath()));
			response = fbClientChild.publish(locationName + "/videos", 
					FacebookType.class, Parameter.with("file_url", post.getImageUrl()),
					Parameter.with("description", post.getSummary()));
		}
		System.out.println("fb.com/"+response.toString());

		return response.getId();
	}
	
	public String uploadFacebookTextPost(String clientId, Post post, String dealerId) throws SQLException {

		FacebookLocation facebookLocation = facebookLocationService.getFacebookLocationByClientIdAndDealerId(clientId, dealerId);

		FacebookClient fbClientChild = new DefaultFacebookClient(facebookLocation.getAccessToken(), Version.LATEST);
		FacebookType response = fbClientChild.publish(facebookLocation.getFacebookPageId()+"/feed", FacebookType.class, 
				Parameter.with("message", post.getSummary())
				);
		System.out.println("fb.com/"+response.toString());

		return response.getId();
	}
	
	public String uploadFacebookCarouselPost(String clientId, Post post, String dealerId) throws SQLException, FileNotFoundException {
		FacebookType response = new FacebookType();
		FacebookLocation facebookLocation = facebookLocationService.getFacebookLocationByClientIdAndDealerId(clientId, dealerId);
		FacebookClient fbClientChild = new DefaultFacebookClient(facebookLocation.getAccessToken(), Version.LATEST);
		String locationName = facebookLocation.getFacebookPageId();

		List<FacebookPostResponse> facebookPostResponse = new ArrayList<>();

		String imagePaths = post.getImageUrl();
		String imagePath[] = imagePaths.split(",");
		for(String image : imagePath) {

			FacebookPostResponse fbPostResponse = FacebookPostResponse.builder()
					.link(post.getActionUrl())
					.picture(image.trim())
					.build();
			facebookPostResponse.add(fbPostResponse);
		}

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		String fbPostImages = gson.toJson(facebookPostResponse);
		System.out.println(fbPostImages);
		response = fbClientChild.publish(locationName + "/feed", 
				FacebookType.class, Parameter.with("child_attachments", fbPostImages),
				Parameter.with("caption", post.getSummary()),
				Parameter.with("message", post.getSummary()),
				Parameter.with("link", post.getActionUrl())
				);
		System.out.println(response.getId());
		return response.getId();
	}
	
	//--------------------------------------------------------------HELPER METHOD-------------------------------------------------------------

	public static String templatize(GMBLocation info, String template) {
		if (template == null) {
			return "";
		}

		Map<String, String> replacementMap = new HashMap<>();
		replacementMap.put(Post.TEMPLATE_TOKEN_AREA, info.getArea());
		replacementMap.put(Post.TEMPLATE_TOKEN_CITY, info.getCity());
		replacementMap.put(Post.TEMPLATE_TOKEN_STATE, info.getState());
		replacementMap.put(Post.TEMPLATE_TOKEN_DEALER_ID, info.getDealerId());
		replacementMap.put(Post.TEMPLATE_TOKEN_MAP_URL, info.getMapUrl());
		replacementMap.put(Post.TEMPLATE_TOKEN_WEBSITE_URL, info.getWebsiteUrl());
		replacementMap.put(Post.TEMPLATE_TOKEN_DEALER_NAME, info.getName());
		replacementMap.put(Post.TEMPLATE_TOKEN_PHONE_NUMBER, info.getPhoneNumber());

		StringBuilder sb = new StringBuilder(template);
		for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
			String token = getDynamicElementName(entry.getKey(), false);
			if (template.contains(token)) {
				String value = StringUtils.isNotBlank(entry.getValue()) ? entry.getValue() : StringUtils.EMPTY;
				String replacementToken = getDynamicElementName(entry.getKey(), true);
				sb.replace(0, sb.length(), sb.toString().replaceAll(replacementToken, WordUtils.capitalize(value)));
			}
		}

		return sb.toString();
	}
	
	private static String getDynamicElementName(String token, boolean includeEscape) {
		String elementName = "";
		if (includeEscape) {
			elementName += "\\";
		}
		elementName += "{";
		elementName += token;
		if (includeEscape) {
			elementName += "\\";
		}
		elementName += "}";

		return elementName;
	}

}

package com.caliper.location.gmb.dto;
import java.util.List;

import com.caliper.location.dto.CategoryServices;
import com.caliper.location.dto.request.LocalListingInfoRequest;
import com.caliper.location.gmb.entity.GMBCategory;


public class GMBInfoRequest extends LocalListingInfoRequest{

	public GMBCategory gmbPrimaryCategory;
	public List<GMBCategory> gmbAdditionalCategories;
	public String appointmentLink;
	public String languageCode;
	public CategoryServices categoryServices;
	public String instagramAttribute;
	public String facebookAttribute;
	public String youtubeAttribute;
	public String twitterAttribute;
	public String whatsAppAttribute;
	public String linkedinAttribute;
	
}

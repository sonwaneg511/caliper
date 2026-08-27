package com.caliper.location.dto.request;

import java.util.List;
import com.caliper.location.dto.CategoryServices;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GMBDetailsBody {

	public Long gmbPrimaryCategory;
	public List<Long> gmbAdditionalCategories;
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
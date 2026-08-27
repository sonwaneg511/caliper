package com.caliper.location.dto;

import java.util.List;

import com.caliper.location.dto.response.CategoryDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FacebookDetailsDto {
    private String businessName;
    private String parentPageId;
    private String fbLocationId;
    private String fbWebsiteUrl;
    private String facebookPageUrl;
    private CategoryDetails fbPrimaryCategoryDetails;
    private List<CategoryDetails> fbAdditionalCategoryDetails;
    private Boolean pagePublishStatus;

}

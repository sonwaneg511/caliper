package com.caliper.adwards.service.bidding;

import com.google.ads.googleads.v21.common.MaximizeConversionValue;
import com.google.ads.googleads.v21.resources.Campaign.Builder;

public class SEMMaximizeConversionValueBiddingStrategy extends SEMBiddingStrategy {
	
	public SEMMaximizeConversionValueBiddingStrategy() { }
	
	public SEMMaximizeConversionValueBiddingStrategy(String value) { }
	
	@Override
	public void setBiddingStrategy(Builder campaignBuilder) {

	//	Builder campaignBuilder = campaign.toBuilder();

		MaximizeConversionValue maximizeConversionValue = MaximizeConversionValue.newBuilder().build();

		campaignBuilder.setMaximizeConversionValue(maximizeConversionValue);

	}

	public String getDisplayName() {
		return "Maximize Conversion Value";
	}
	
	public String getHtmlName() {
		return BIDDING_STRATEGY_MAXIMIZE_CONVERSION_VALUE;
	}
	
	public String getHtmlCode(String value) {
		return "";
	}
	
	@Override
	public boolean inSearch() {
		return true;
	}

	@Override
	public boolean inDisplay() {
		return false;
	}

	@Override
	public boolean inShopping() {
		return false;
	}

	@Override
	public boolean inApp() {
		return false;
	}

	@Override
	public boolean inSmartDisplay() {
		return false;
	}

	@Override
	public boolean inGmail() {
		return true;
	}
}


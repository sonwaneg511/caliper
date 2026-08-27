package com.caliper.adwards.service.bidding;

import com.google.ads.googleads.v21.common.MaximizeConversions;
import com.google.ads.googleads.v21.resources.Campaign.Builder;

public class SEMMaximizeConversionsBiddingStrategy extends SEMBiddingStrategy {
	
	public SEMMaximizeConversionsBiddingStrategy() { }
	
	public SEMMaximizeConversionsBiddingStrategy(String value) { }
	
	@Override
	public void setBiddingStrategy(Builder campaignBuilder) {

	//	Builder campaignBuilder = campaign.toBuilder();

		MaximizeConversions maximiseConversions = MaximizeConversions.newBuilder().build();

		campaignBuilder.setMaximizeConversions(maximiseConversions);
	}

	public String getDisplayName() {
		return "Maximize Conversions";
	}
	
	public String getHtmlName() {
		return BIDDING_STRATEGY_MAXIMIZE_CONVERSIONS;
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
		return true;
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


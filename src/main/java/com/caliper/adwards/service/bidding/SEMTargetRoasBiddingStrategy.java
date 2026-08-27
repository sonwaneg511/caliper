package com.caliper.adwards.service.bidding;

import com.google.ads.googleads.v21.common.TargetRoas;
import com.google.ads.googleads.v21.resources.Campaign.Builder;

public class SEMTargetRoasBiddingStrategy extends SEMBiddingStrategy {
	
	private double targetRoasValue;
	
	public SEMTargetRoasBiddingStrategy() { }
	
	public SEMTargetRoasBiddingStrategy(String value) {
		this.targetRoasValue = Double.parseDouble(getAmountMicros(value));
	}
	
	@Override
	public void setBiddingStrategy(Builder campaignBuilder) {
		
	//	Builder campaignBuilder = campaign.toBuilder();
		
		TargetRoas targetRoas = TargetRoas.newBuilder().setTargetRoas(Double.valueOf(targetRoasValue)).build();
		
		campaignBuilder.setTargetRoas(targetRoas);
	}

	public String getDisplayName() {
		return "Target ROAS";
	}
	
	public String getHtmlName() {
		return BIDDING_STRATEGY_TARGET_ROAS;
	}
	
	public String getHtmlCode(String value) {
		String htmlCode = "Target ROAS % : <input type=\"text\" " +
					"name=\"" + BIDDING_STRATEGY_VALUE_HTML_NAME + "\" " +
					"id=\"" + BIDDING_STRATEGY_VALUE_HTML_NAME + "\" " +
					((value != null) ? "value = " + value : "") +
					" >";
		return htmlCode;
	}
	
	@Override
	public boolean inSearch() {
		return false;
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
		return false;
	}
}


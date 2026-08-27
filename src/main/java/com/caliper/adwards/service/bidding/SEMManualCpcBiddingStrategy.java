package com.caliper.adwards.service.bidding;

import com.google.ads.googleads.v21.common.ManualCpc;
import com.google.ads.googleads.v21.resources.Campaign.Builder;

public class SEMManualCpcBiddingStrategy extends SEMBiddingStrategy {
	
	private boolean enhancedCpc;
	
	public SEMManualCpcBiddingStrategy() { }
	
	public SEMManualCpcBiddingStrategy(String value) {
		try {
			this.enhancedCpc = Boolean.parseBoolean(value);
		} catch (Exception ex) {
			this.enhancedCpc = false;
		}
	}
	
	public void setBiddingStrategy(Builder campaignBuilder) {
		
	//	Builder campaignBuilder = campaign.toBuilder();

		ManualCpc manualCpc = ManualCpc.newBuilder().setEnhancedCpcEnabled(enhancedCpc).build();

		campaignBuilder.setManualCpc(manualCpc).build();

	}
	
	public String getDisplayName() {
		return "Manual CPC";
	}
	
	public String getHtmlName() {
		return BIDDING_STRATEGY_MANUAL_CPC;
	}
	
	public String getHtmlCode(String value) {
		String html = "Enhanced CPC : <input type=\"checkbox\" " +
				"name=\"" + BIDDING_STRATEGY_VALUE_HTML_NAME + "\" " +
				"id=\"" + BIDDING_STRATEGY_VALUE_HTML_NAME + "\" " +
				((value != null) ? " checked" : "") +
				" >";
		return html;
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

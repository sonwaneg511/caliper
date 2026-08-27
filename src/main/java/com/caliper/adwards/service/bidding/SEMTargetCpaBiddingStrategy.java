package com.caliper.adwards.service.bidding;

import com.google.ads.googleads.v21.common.TargetCpa;
import com.google.ads.googleads.v21.resources.Campaign.Builder;

public class SEMTargetCpaBiddingStrategy extends SEMBiddingStrategy {
	
	private double targetCpaValue;
	
	public SEMTargetCpaBiddingStrategy() { }
	
	public SEMTargetCpaBiddingStrategy(String value) {
		this.targetCpaValue = Double.parseDouble(getAmountMicros(value));
	}
	
	@Override
	public void setBiddingStrategy(Builder campaignBuilder) {

		//Builder campaignBuilder = campaign.toBuilder();
		
		TargetCpa targetCpa = TargetCpa.newBuilder().setTargetCpaMicros(Double.valueOf(targetCpaValue).longValue())
				.build();

		campaignBuilder.setTargetCpa(targetCpa);

	}

	public String getDisplayName() {
		return "Target CPA";
	}
	
	public String getHtmlName() {
		return BIDDING_STRATEGY_TARGET_CPA;
	}
	
	public String getHtmlCode(String value) {
		String html = "Target CPA : <input type=\"text\" " +
				"name=\"" + BIDDING_STRATEGY_VALUE_HTML_NAME + "\" " +
				"id=\"" + BIDDING_STRATEGY_VALUE_HTML_NAME + "\" " +
				((value != null) ? "\" value = \"" + value + "\"" : "") +
				">";
		String x=  "\""+value+"\"";
		System.out.println(x);
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

package com.caliper.adwards.service.bidding;

import com.google.ads.googleads.v21.common.TargetSpend;
import com.google.ads.googleads.v21.resources.Campaign.Builder;

public class SEMMaximizeClicksBiddingStrategy extends SEMBiddingStrategy {
	
	private double maxCpcBidLimit;
	
	public SEMMaximizeClicksBiddingStrategy() { }
	
	public SEMMaximizeClicksBiddingStrategy(String value) {
		
		this.maxCpcBidLimit = Double.parseDouble(getAmountMicros(value));
	}
	
	@Override
	public void setBiddingStrategy(Builder campaignBuider) {

	//	Builder campaignBuider = campaign.toBuilder();

		TargetSpend targetSpend = TargetSpend.newBuilder()
				.setCpcBidCeilingMicros(Double.valueOf(maxCpcBidLimit).longValue()).build();

		campaignBuider.setTargetSpend(targetSpend);

	}

	public String getDisplayName() {
		return "Maximize Clicks";
	}
	
	public String getHtmlName() {
		return BIDDING_STRATEGY_MAXIMIZE_CLICKS;
	}
	
	public String getHtmlCode(String value) {
		value = (value == null) ? "-1" : value;
		String html = "Max CPC Bid Limit (Put -1 to not apply) : <input type=\"text\" name=\"" + BIDDING_STRATEGY_VALUE_HTML_NAME + "\" id=\"" + BIDDING_STRATEGY_VALUE_HTML_NAME + "\" value = \"" + value + "\" >";
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
		return false;
	}
}


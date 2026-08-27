package com.caliper.adwards.service.bidding;

import com.google.ads.googleads.v21.common.TargetImpressionShare;
import com.google.ads.googleads.v21.resources.Campaign.Builder;

public class SEMTargetImpressionShareBiddingStrategy extends SEMBiddingStrategy {
		
	private long targetImressionValue;

	public SEMTargetImpressionShareBiddingStrategy() {
		
	}
	public SEMTargetImpressionShareBiddingStrategy(String  value) {
		this.targetImressionValue = Long.parseLong(getAmountMicros(value));
	}
	@Override
	public void setBiddingStrategy(Builder campaignBuilder) {
		
//		Builder campaignBuilder = campaign.toBuilder();
		
		TargetImpressionShare targetImpressionShare = TargetImpressionShare.newBuilder().
				setCpcBidCeilingMicros(targetImressionValue).build();
				
		campaignBuilder.setTargetImpressionShare(targetImpressionShare);
		
	}
	@Override
	public String getDisplayName() {
		return "Target Impression Share";
	}
	@Override
	public String getHtmlName() {
		return BIDDING_STRATEGY_TARGET_IMPRESSION_SHARE;
	}
	@Override
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


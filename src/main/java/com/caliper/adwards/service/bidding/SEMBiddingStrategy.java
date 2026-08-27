package com.caliper.adwards.service.bidding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.ads.googleads.v21.resources.Campaign.Builder;
import com.google.gson.annotations.Expose;

public abstract class SEMBiddingStrategy {
	
	public static final String BIDDING_STRATEGY_HTML_NAME = "bidding_strategy_name";
	public static final String BIDDING_STRATEGY_VALUE_HTML_NAME = "bidding_strategy_value";
	
	@Expose
	public static final String BIDDING_STRATEGY_MANUAL_CPC = "manual_cpc";
	public static final String BIDDING_STRATEGY_TARGET_CPA = "target_cpa";
	public static final String BIDDING_STRATEGY_TARGET_ROAS = "target_roas";
	@Expose
	public static final String BIDDING_STRATEGY_MAXIMIZE_CLICKS = "maximize_clicks";
	@Expose
	public static final String BIDDING_STRATEGY_MAXIMIZE_CONVERSIONS = "maximize_conversions";
	@Expose
	public static final String BIDDING_STRATEGY_MAXIMIZE_CONVERSION_VALUE = "maximize_conversion_value";
	public static final String BIDDING_STRATEGY_INSTALL_VOLUME = "install_volume";
	public static final String BIDDING_STRATEGY_TARGET_IMPRESSION_SHARE = "target_impression_share";
	public static final String BIDDING_STRATEGY_PORTFOLIO = "portfolio";
	public static final String BIDDING_STRATEGY_PORTFOLIO_MAXIMIZE_CONVERSIONS = "portfolio_maximize_clicks";
	public static final String BIDDING_STRATEGY_PORTFOLIO_TARGET_ROAS = "portfolio_target_roas";
	public static final String BIDDING_STRATEGY_PORTFOLIO_TARGET_CPA = "portfolio_target_cpa";
	
	public abstract void setBiddingStrategy(Builder campaignBuilder);
	public abstract String getDisplayName();
	public abstract String getHtmlName();
	public abstract String getHtmlCode(String value);
	public abstract boolean inSearch();
	public abstract boolean inDisplay();
	public abstract boolean inShopping();
	public abstract boolean inApp();
	public abstract boolean inSmartDisplay();
	public abstract boolean inGmail();
	//public abstract boolean inDynamicSearchAd();
	
	public static String getAmountMicros(String value) {
		
		String microsAmount = value+"000000";
		
		return microsAmount;
	}
	
	public SEMBiddingStrategy() {
		super();
	}
	
	private static List<SEMBiddingStrategy> getAllBiddingStrategies() {
		List<SEMBiddingStrategy> strategies = new ArrayList<SEMBiddingStrategy>();
		strategies.add(new SEMManualCpcBiddingStrategy());
		strategies.add(new SEMTargetCpaBiddingStrategy());
		strategies.add(new SEMTargetRoasBiddingStrategy());
		strategies.add(new SEMMaximizeClicksBiddingStrategy());
		strategies.add(new SEMMaximizeConversionsBiddingStrategy());
		strategies.add(new SEMMaximizeConversionValueBiddingStrategy());
		return strategies;
	}
	
	public static String getJSMapFormat(String value) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(BIDDING_STRATEGY_MANUAL_CPC, new SEMManualCpcBiddingStrategy().getHtmlCode(value));
		map.put(BIDDING_STRATEGY_TARGET_CPA, new SEMTargetCpaBiddingStrategy().getHtmlCode(value));
		map.put(BIDDING_STRATEGY_TARGET_ROAS, new SEMTargetRoasBiddingStrategy().getHtmlCode(value));
		map.put(BIDDING_STRATEGY_MAXIMIZE_CLICKS, new SEMMaximizeClicksBiddingStrategy().getHtmlCode(value));
		map.put(BIDDING_STRATEGY_MAXIMIZE_CONVERSIONS, new SEMMaximizeConversionsBiddingStrategy().getHtmlCode(value));
		map.put(BIDDING_STRATEGY_MAXIMIZE_CONVERSION_VALUE, new SEMMaximizeConversionValueBiddingStrategy().getHtmlCode(value));
		StringBuilder sb = new StringBuilder();
		String ip = map.toString();
		ip = ip.replace("{", "").replace("}", "");
		String ipArray[] = ip.split(",");
 		sb.append("[");
		for(String token : ipArray) {
			sb.append("[");
			String x = token.replaceFirst("=", "?");
			String [] arr = x.split("\\?");
			for(String innerToken : arr) {
				sb.append("'"+innerToken.trim()+"'"+",");
			}
			sb.append("], ");
		}
		sb.append("]");
		String finalString = sb.toString();
		finalString = finalString.replace("',]", "']");
		finalString = finalString.replace(", ]", "]");
		return finalString;
	}
}


package com.caliper.adwards.service.bidding;


public class SEMBiddingStrategyFactory {
	
	public static SEMBiddingStrategy getSEMBiddingStrategy(String name, String value) {
		if (SEMBiddingStrategy.BIDDING_STRATEGY_MANUAL_CPC.equalsIgnoreCase(name)) {
			return new SEMManualCpcBiddingStrategy(value);
		} else if (SEMBiddingStrategy.BIDDING_STRATEGY_TARGET_CPA.equalsIgnoreCase(name)) {
			return new SEMTargetCpaBiddingStrategy(value);
		} else if (SEMBiddingStrategy.BIDDING_STRATEGY_MAXIMIZE_CLICKS.equalsIgnoreCase(name)) {
			return new SEMMaximizeClicksBiddingStrategy(value);
		} else if (SEMBiddingStrategy.BIDDING_STRATEGY_MAXIMIZE_CONVERSIONS.equalsIgnoreCase(name)) {
			return new SEMMaximizeConversionsBiddingStrategy(value);
		} else if (SEMBiddingStrategy.BIDDING_STRATEGY_MAXIMIZE_CONVERSION_VALUE.equalsIgnoreCase(name)) {
			return new SEMMaximizeConversionValueBiddingStrategy(value);
		} else if (SEMBiddingStrategy.BIDDING_STRATEGY_TARGET_ROAS.equalsIgnoreCase(name)) {
			return new SEMTargetRoasBiddingStrategy(value);
		}else if (SEMBiddingStrategy.BIDDING_STRATEGY_TARGET_IMPRESSION_SHARE.equalsIgnoreCase(name)) {
			return new SEMTargetImpressionShareBiddingStrategy(value);
		}
		return null;
	}
}
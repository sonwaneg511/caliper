package com.caliper.utils.campaign;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;

import com.google.ads.googleads.lib.GoogleAdsClient;

public class GoogleSessionFactory {

	@Value("${google.ads.properties.path}")
	private static String adsPropertiesPath;

	@Value("${google.ads.default-login-customer-id}")
	private static Long defaultLoginCustomerId;
	
	public static GoogleAdsClient getGoogleAdsClient() throws IOException {
		File propertiesFile = new File(adsPropertiesPath);
		return GoogleAdsClient.newBuilder()
				.fromPropertiesFile(propertiesFile)
				.setLoginCustomerId(defaultLoginCustomerId)
				.build();
	}
	
	public static GoogleAdsClient getGoogleAdsClientByLoginCustomerID(Long loginCustomerId) throws IOException {
		File propertiesFile = new File(adsPropertiesPath);
		return GoogleAdsClient.newBuilder()
				.fromPropertiesFile(propertiesFile)
				.setLoginCustomerId(loginCustomerId)
				.build();
	}
}

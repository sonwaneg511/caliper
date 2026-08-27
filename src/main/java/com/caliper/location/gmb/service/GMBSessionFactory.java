package com.caliper.location.gmb.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.caliper.bigquery.service.ProjectDataHelper;
import com.caliper.location.gmb.entity.GMBOAuthToken;
import com.caliper.location.gmb.repository.GMBOAuthTokenRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.businessprofileperformance.v1.BusinessProfilePerformance;
import com.google.api.services.mybusiness.v4.MyBusiness;
import com.google.api.services.mybusinessaccountmanagement.v1.MyBusinessAccountManagement;
import com.google.api.services.mybusinessbusinessinformation.v1.MyBusinessBusinessInformation;
import com.google.api.services.mybusinessplaceactions.v1.MyBusinessPlaceActions;
import com.google.api.services.mybusinessverifications.v1.MyBusinessVerifications;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;

//@PropertySource("classpath:platform-application.properties")
@PropertySource("classpath:application-${spring.profiles.active}.properties")
@Configuration
public class GMBSessionFactory {

	private static final HttpTransport HTTP_TRANSPORT = Utils.getDefaultTransport();
	private static final JsonFactory JSON_FACTORY = Utils.getDefaultJsonFactory();
	private static final String APPLICATION_NAME = "IA Application";

	@Value("${gmb.data.dir}")
	public String dataDir;

	@Value("${gmb.cred.dir}")
	public String credDir;

	@Autowired
	private ProjectDataHelper dataHelper;

	@Autowired
	private GMBOAuthTokenRepository gmbOAuthTokenRepository;

//	private HttpRequestInitializer buildCredential(String clientId) throws IOException {
//		GMBOAuthToken token = (clientId != null) ? gmbOAuthTokenRepository.findByClientId(clientId)
//				: null;//remove
//		if (token == null) {
//			Credential credential = getCredential(null);
//			//return new HttpCredentialsAdapter(credential);
////			throw new IllegalStateException(
////					"No GMB OAuth token found" + (clientId != null ? " for clientId=" + clientId : ""));
//		}
//		AccessToken accessToken = new AccessToken(token.getAccessToken(), token.getTokenExpiry());
//		UserCredentials credentials = UserCredentials.newBuilder().setClientId(token.getGcpClientId())
//				.setClientSecret(token.getGcpClientSecret()).setAccessToken(accessToken)
//				.setRefreshToken(token.getRefreshToken()).build();
//		return new HttpCredentialsAdapter(credentials);
//	}

	private HttpRequestInitializer resolveCredential(String clientId) throws IOException {
		GMBOAuthToken token = (clientId != null)
				? gmbOAuthTokenRepository.findByClientId(clientId)
				: null;
		if (token != null) {
			AccessToken accessToken = new AccessToken(token.getAccessToken(), token.getTokenExpiry());
			UserCredentials credentials = UserCredentials.newBuilder().setClientId(token.getGcpClientId())
					.setClientSecret(token.getGcpClientSecret()).setAccessToken(accessToken)
					.setRefreshToken(token.getRefreshToken()).build();
			return new HttpCredentialsAdapter(credentials);
		}
		return getCredential(clientId);
	}

	private Credential getCredential(String clientName) throws IOException {

		FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(credDir));
		if (clientName != null) {
			dataStoreFactory = new FileDataStoreFactory(new File(credDir));
		}

		// Creates an InputStream to hold the client ID and secret.
		// InputStream secrets =
		// dataHelper.getJsonFile(ProjectDataHelper.CLIENT_SECRET);

		// Uses the InputStream to create an instance of GoogleClientSecrets.
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
				new InputStreamReader(new FileInputStream(dataDir)));
		if (clientSecrets.getDetails().getClientId().startsWith("Enter")
				|| clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
			/*
			 * System.out.println("Enter Client ID and Secret from Google API Console " +
			 * "into google-my-business-api-sample/src/main/resources/client_secrets.json");
			 */
			System.exit(1);
		}

		// Sets up the authorization code flow.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, Collections.singleton("https://www.googleapis.com/auth/business.manage"))
				.setDataStoreFactory(dataStoreFactory).setAccessType("offline").build();
		// Returns the credential.
		Credential credential = new AuthorizationCodeInstalledApp(flow,
				new LocalServerReceiver.Builder().setPort(9090).setCallbackPath("/web/pages/test.jsp").build())
				.authorize("user");
		return credential;
	}

	public BusinessProfilePerformance getMyBusinessProfile() throws IOException {
		HttpRequestInitializer credential = resolveCredential(null);
		BusinessProfilePerformance business = new BusinessProfilePerformance.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				credential).setApplicationName(APPLICATION_NAME).build();
		return business;
	}

	public BusinessProfilePerformance getMyBusinessProfile(String clientId) throws IOException {
		HttpRequestInitializer credential = resolveCredential(clientId);
		return new BusinessProfilePerformance.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}

	public MyBusinessVerifications getGMBLocationVerificationSession() throws FileNotFoundException, IOException {
		HttpRequestInitializer credential = resolveCredential(null);
		MyBusinessVerifications business = new MyBusinessVerifications.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
		return business;
	}

	public MyBusinessVerifications getGMBLocationVerificationSession(String clientId) throws IOException {
		HttpRequestInitializer credential = resolveCredential(clientId);
		return new MyBusinessVerifications.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}

	public MyBusinessBusinessInformation getGMBLocationSession() throws FileNotFoundException, IOException {
		HttpRequestInitializer credential = resolveCredential(null);
		MyBusinessBusinessInformation business = new MyBusinessBusinessInformation.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				credential).setApplicationName(APPLICATION_NAME).build();
		return business;
	}

	public MyBusinessBusinessInformation getGMBLocationSession(String clientId) throws IOException {
		HttpRequestInitializer credential = resolveCredential(clientId);
		return new MyBusinessBusinessInformation.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}

	public MyBusinessAccountManagement getGMBAccountSession() throws FileNotFoundException, IOException {
		HttpRequestInitializer credential = resolveCredential(null);
		MyBusinessAccountManagement business = new MyBusinessAccountManagement.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				credential).setApplicationName(APPLICATION_NAME).build();
		return business;
	}

	public MyBusiness getGMBSession() throws FileNotFoundException, IOException {
		HttpRequestInitializer credential = resolveCredential(null);
		MyBusiness business = new MyBusiness.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
		return business;
	}

	public MyBusinessPlaceActions getPlaceActionBusiness() throws FileNotFoundException, IOException {
		HttpRequestInitializer credential = resolveCredential(null);
		MyBusinessPlaceActions business = new MyBusinessPlaceActions.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
		return business;
	}

	public MyBusiness getGMBSession(String clientId) throws IOException {
		HttpRequestInitializer credential = resolveCredential(clientId);
		return new MyBusiness.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	public MyBusinessAccountManagement getGMBAccountSession(String clientId) throws IOException {
		HttpRequestInitializer credential = resolveCredential(clientId);
		return new MyBusinessAccountManagement.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}

}

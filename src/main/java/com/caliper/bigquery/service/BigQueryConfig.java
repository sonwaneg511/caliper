package com.caliper.bigquery.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

@Configuration
public class BigQueryConfig {

	@Autowired
    private ProjectDataHelper projectDataHelper;

	private static BigQuery bigQuery;
	private static Map<String,String> projectMap;
	private static Map<String,BigQuery> bigQueryMap = new HashMap<String,BigQuery>();
	public static String BQ_PROJECT_NAME_IA_REPORTS = "ia-reports-123456";
	public static String BQ_PROJECT_NAME_ADVANCED_ANALYTICS = "advanced-analytics-123456";
	public static String BQ_PROJECT_NAME_REPORTING_AUTOMATION = "reporting-automation-123456";
	public static String BQ_PROJECT_NAME_TEST = "test-123456";
	public static String BQ_PROJECT_NAME_CARTER = "carter-123456";
	public static String BQ_PROJECT_NAME_DSP_REPORTING = "ia-dsp-123456";
	public static String BQ_PROJECT_NAME_BUZZ = "buzz-123456";
	
	static {
		projectMap = new HashMap<String,String>();
		projectMap.put(BQ_PROJECT_NAME_IA_REPORTS, ProjectDataHelper.IA_REPORTS);
		projectMap.put(BQ_PROJECT_NAME_ADVANCED_ANALYTICS, ProjectDataHelper.ADVANCED_ANALYTICS);
		projectMap.put(BQ_PROJECT_NAME_REPORTING_AUTOMATION, ProjectDataHelper.REPORTING_AUTOMATION);
		projectMap.put(BQ_PROJECT_NAME_TEST, ProjectDataHelper.TEST);
		projectMap.put(BQ_PROJECT_NAME_CARTER, ProjectDataHelper.CARTER);
		projectMap.put(BQ_PROJECT_NAME_DSP_REPORTING, ProjectDataHelper.DSP_REPORTING);
		projectMap.put(BQ_PROJECT_NAME_BUZZ, ProjectDataHelper.BUZZ);
	}

    
    public BigQuery getBigQueryDao() throws FileNotFoundException, IOException {
		if (bigQuery != null) {
			return bigQuery;
		}
		
		Map<String, String> projectData = projectDataHelper.loadProjectData();
		try(InputStream bqCredentialsFile = projectDataHelper.getJsonFile(projectData.get(ProjectDataHelper.IA_REPORTS))){
			bigQuery = BigQueryOptions.newBuilder()
					.setCredentials(ServiceAccountCredentials.fromStream(
							bqCredentialsFile))
					.build()
					.getService();
		}
		return bigQuery;
	}
  
    
    public BigQuery getBigQueryDao(String projectName) throws FileNotFoundException, IOException {
    	projectDataHelper.loadProjectData();
    	if (bigQueryMap.containsKey(projectName)) {
			return bigQueryMap.get(projectName);
		}
		
		if(projectMap.containsKey(projectName)) {
			BigQuery bq = null;
			try(InputStream bQCredentialsFile = projectDataHelper.getJsonFile(projectMap.get(projectName))){
				bq =  BigQueryOptions.newBuilder()
						.setCredentials(ServiceAccountCredentials.fromStream(
								bQCredentialsFile))
						.build()
						.getService();
			}
			bigQueryMap.put(projectName, bq);
			return bq;
		}
		return null;
	}
    
//    public BigQuery getBigQueryDao(String projectName) throws FileNotFoundException, IOException {
//    	Map<String, String> projectData = projectDataHelper.loadProjectData();
//    	  if (bigQueryMap.containsKey(projectName)) {
//    	   return bigQueryMap.get(projectName);
//    	  }
//    	  
//    	  if(projectMap.containsKey(projectName)) {
//    		  String dbUrl = projectData.get(projectMap.get(projectName));
//    	  // BigQuery bq = null;
//    	   URL url = new URL(dbUrl);
//
//    	   try (InputStream inputStream = url.openStream()) {
//    	       BigQuery bq = BigQueryOptions.newBuilder()
//    	           .setCredentials(ServiceAccountCredentials.fromStream(inputStream))
//    	           .build()
//    	           .getService();
//
//    	       bigQueryMap.put(projectName, bq);
//    	       return bq;
//    	   }
//    	  }
//    	  return null;
//    	 }
}
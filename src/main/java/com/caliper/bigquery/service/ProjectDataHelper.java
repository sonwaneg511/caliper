package com.caliper.bigquery.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.caliper.bigquery.entity.ProjectData;
import com.caliper.bigquery.repository.ProjectDataRepository;
import com.caliper.utils.exception.customException.ResourceNotFoundException;


/**
 * Utility class to provide project JSON credentials for BigQuery.
 */
@Service
public class ProjectDataHelper {

    private final ProjectDataRepository projectDataRepository;
    private final Map<String, String> projectMap = new HashMap<>();

    public static final String IA_REPORTS = "ia_reports";
    public static final String ADVANCED_ANALYTICS = "advanced_analytics";
    public static final String REPORTING_AUTOMATION = "reporting_automation";
    public static final String TEST = "test";
    public static final String CARTER = "carter";
    public static final String DSP_REPORTING = "dsp_reporting";
    public static final String BUZZ = "buzz";
    public static final String GMB_IA = "gmb_ia";
	public static final String CLIENT_SECRET = "client_secret";


    public ProjectDataHelper(ProjectDataRepository projectDataRepository) {
        this.projectDataRepository = projectDataRepository;
    }

    public Map<String, String> loadProjectData() {
        List<ProjectData> projectList = projectDataRepository.findAll();
        if (projectList != null) {
            for (ProjectData project : projectList) {
                projectMap.put(project.getProjectName(), project.getData());
            }
        }
        return projectMap;
    }

    public InputStream getJsonFile(String projectName) throws UnsupportedEncodingException {
    	loadProjectData();
    	String json = projectMap.get(projectName);
        if (json == null)
            throw new ResourceNotFoundException("Project JSON not found: " + projectName);

        return new ByteArrayInputStream(json.getBytes("UTF-8"));
    }
}
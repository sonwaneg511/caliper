package com.caliper.utils.projectDataHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import com.caliper.bigquery.entity.ProjectData;
import com.caliper.bigquery.repository.ProjectDataRepository;
import jakarta.annotation.PostConstruct;

@Service
public class ProjectDataService {

	private static final Logger logger = LoggerFactory.getLogger(ProjectDataService.class);
    private final ProjectDataRepository projectDataRepository;
    private final Map<String, String> projectMap = new ConcurrentHashMap<>();

    // Constants for project names
    public static final String IA_REPORTS = "ia_reports";
    public static final String ADVANCED_ANALYTICS = "advanced_analytics";
    public static final String REPORTING_AUTOMATION = "reporting_automation"; 
    public static final String TEST = "test";
    public static final String CARTER = "carter";
    public static final String DSP_REPORTING = "dsp_reporting";
    public static final String BUZZ = "buzz";
    public static final String GCP_TEST = "gcp_test";
    public static final String PULSE = "pulse";
    public static final String CONCRETE_CROW = "concrete_crow";
    public static final String TRADING_DATA_STORAGE = "trading_data_storage";
    public static final String E_STONE = "e_stone";
    public static final String NEON_VICTORY = "neon_victory";
    public static final String DATATECH_ACT = "datatech_act";
    public static final String DATATECH_MAHINDRA = "datatech_mahindra";
    public static final String DATATECH_GARWARE = "datatech_garware";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String GMB_IA = "gmb_ia";

    // Constructor injection
    public ProjectDataService(ProjectDataRepository projectDataRepository) {
        this.projectDataRepository = projectDataRepository;
    }

    // Load project data on startup
    @PostConstruct
    public void loadProjectData() {
            List<ProjectData> projectList = projectDataRepository.findAll();
            if (projectList != null && !projectList.isEmpty()) {
                for (ProjectData project : projectList) {
                    projectMap.put(project.getProjectName(), project.getData());
                    logger.info("Loaded {} projects into memory cache.", projectList.size());
                }
            } else {
            	logger.warn("Project list is empty!");
            }
    }

    // Get JSON as InputStream
    public Optional<InputStream> getJsonFile(String projectName) {
        return Optional.ofNullable(projectMap.get(projectName))
                       .map(data -> new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
    }

    // Optional: Reload data manually
    public void reloadProjectData() {
        projectMap.clear();
        loadProjectData();
        logger.info("Project data reloaded successfully.");
    }
}

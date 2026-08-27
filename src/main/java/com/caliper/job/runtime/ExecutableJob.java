package com.caliper.job.runtime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Interface that all scheduled job classes must implement.
 *
 * Purpose: This is the contract that the scheduler uses to execute jobs.
 * When a scheduled job runs, the scheduler will:
 * 1. Load the class specified in className
 * 2. Create an instance of that class
 * 3. Call the run() method with the job parameters
 *
 * Usage Example:
 *
 * public class ReportJob implements ExecutableJob {
 *     @Override
 *     public void run(Map<String, String> params) {
 *         String client = params.get("client");
 *         String method = params.get("method");
 *         // Your job logic here
 *         System.out.println("Running report for client: " + client);
 *     }
 * }
 *
 * Then create a job with className = "com.caliper.jobs.ReportJob"
 */
public interface ExecutableJob {

    /**
     * Method that will be called when the scheduled job executes.
     *
     * @param params Map of parameters passed to the job
     *               These are the jobParameters from CreateJobRequest
     *               Example: {"client": "acme", "method": "runReport"}
     * @throws Exception 
     */
    
  //  Logger log = LoggerFactory.getLogger(ExecutableJob.class);

    void run(Map<String, String> params) throws Exception;
    
    
    default void log(String message) {
    //	 log.info(message);
    }
    
    default void logStart(Map<String, String> params) {
      //  log.info("Executing job with params: {}", params);
    }

    default void logEnd() {
      //  log.info("Job execution completed.");
    }
}




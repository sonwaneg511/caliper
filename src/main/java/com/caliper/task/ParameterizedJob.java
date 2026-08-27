package com.caliper.task;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caliper.jobs.utils.JobParameters;


public abstract class ParameterizedJob implements Runnable {
    
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected JobParameters parameters;
	protected String logFile;  // Log file path will be injected from scheduler
    protected static volatile boolean stopRequested = false; // Shared stop flag for all jobs

    public void setParameters(String xml) throws Exception {
        this.parameters = new JobParameters(xml); 
    }
    
    public void setLogFile(String filePath) {
        this.logFile = filePath;
    }

    public static void requestStop() {
        stopRequested = true;
    }

    public static void resetStop() {
        stopRequested = false;
    }

    protected boolean shouldStop() {
        return stopRequested;
    }
    
    public void log(String message) {
        if (logFile == null) {
            System.err.println("Log file not set.");
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
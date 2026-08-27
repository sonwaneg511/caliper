package com.caliper.task;

import org.springframework.stereotype.Component;

@Component
public class Test extends ParameterizedJob{

	 public int noOfCount;

	    @Override
	    public void run() {
	    	init();
	        resetStop(); // Reset before starting
	        iteratingtask();
	    }

		private void iteratingtask() {
		     	log("Job started...");
			        for (int i = 1; i <= noOfCount; i++) {
			            if (stopRequested) {
			            	log("Job stop requested at iteration " + i);
			                return;
			            }
			            try {
			            log("Running iteration " + i);
			            }catch (Exception e) {
						}
			        }
			        log("Job finished.");
		}

		private void init() {
			this.noOfCount = parameters.getInt("no-of-count", 100);
		}
}

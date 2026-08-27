package com.caliper.jobs.service;

import com.caliper.jobs.entity.ScheduledJobDetail;

public interface JobService {

	public ScheduledJobDetail saveConfig(ScheduledJobDetail config);
	public void scheduleJob(ScheduledJobDetail config);
	public void runNow(Long id);
	public void stopNow(Long id);
	public boolean isRunning(Long id);
}

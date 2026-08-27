package com.caliper.location.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean("clientTaskExecutor")
	public Executor clientTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);     // no. of clients parallel...could be changed in future as per no of clients
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("gmb-client-");
		executor.initialize();
		return executor;
	}
	
	@Bean("gmbInsightTaskExecutor")
	public Executor gmbInsightTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);     // no. of clients parallel...could be changed in future as per no of clients
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("gmb-client-");
		executor.initialize();
		return executor;
	}

	// Dedicated pool for the synchronous /locations/import-location preview fetch,
	// kept separate from clientTaskExecutor so this on-demand call can't starve
	// (or be starved by) the background GMBDeploymentTask import job.
	@Bean("gmbImportPreviewExecutor")
	public Executor gmbImportPreviewExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setMaxPoolSize(6);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("gmb-import-preview-");
		executor.initialize();
		return executor;
	}
}

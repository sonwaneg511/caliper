package com.caliper.jobs.serviceImpl;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

import com.caliper.jobs.entity.ScheduledJobDetail;
import com.caliper.jobs.repository.ScheduledJobDetailRepository;
import com.caliper.jobs.service.JobService;
import com.caliper.task.ParameterizedJob;


@Service
public class JobServiceImpl implements JobService {


	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private ScheduledJobDetailRepository repo;

	@Autowired
	private TaskScheduler scheduler;
	
	@Value("${log.path}")
	private String logPath;

	// Map to track scheduled cron jobs
	private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

	// Map to track one-time or manually running jobs
	private final Map<Long, CompletableFuture<?>> runningTasks = new ConcurrentHashMap<>();

	@Override
	public ScheduledJobDetail saveConfig(ScheduledJobDetail config) {
		ScheduledJobDetail saved = repo.save(config);
		if (!saved.isOneTime()) {
			scheduleJob(saved);
		}
		return saved;
	}

	@Override
	public void scheduleJob(ScheduledJobDetail config) {
	    Runnable task = () -> runJob(config);

	    // Repeat every X minutes
	    if (config.getDelayMinutes() != null && config.getDelayMinutes() > 0) {

	        long periodMillis = config.getDelayMinutes() * 60 * 1000L;
	        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, periodMillis);

	        scheduledTasks.put(config.getId(), future);

	        Date nextExecutionTime = new Date(System.currentTimeMillis() + periodMillis);
	        config.setNextRun(nextExecutionTime);
	        repo.save(config);

	    } 
	    // Recurring via cron
	    else if (config.getCronExpression() != null && !config.getCronExpression().isEmpty()) {

	        CronTrigger cronTrigger = new CronTrigger(config.getCronExpression());
	        ScheduledFuture<?> future = scheduler.schedule(task, cronTrigger);

	        scheduledTasks.put(config.getId(), future);

	        SimpleTriggerContext triggerContext = new SimpleTriggerContext();
	        Date nextExecutionTime = cronTrigger.nextExecutionTime(triggerContext);

	        if (nextExecutionTime != null) {
	            config.setNextRun(nextExecutionTime);
	            repo.save(config);
	        }
	    }
	}



	@Override
	public void runNow(Long id) {
		ScheduledJobDetail config = repo.findById(id).orElseThrow();

		// If already running, do nothing
		if (runningTasks.containsKey(id) && !runningTasks.get(id).isDone()) {
			return;
		}

		CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
			try {
				runJob(config);
			} catch (Exception e) {
				System.err.println("Error executing job: " + e.getMessage());
				e.printStackTrace();
			}
		});

		runningTasks.put(id, future);
	}

	@Override
	public void stopNow(Long id) {
		ScheduledJobDetail config = repo.findById(id).orElseThrow();

		try {
			Class<?> clazz = Class.forName(config.getClassName());
			clazz.getMethod("requestStop").invoke(null);  // Calls static method
			System.out.println("Stop requested for job ID: " + id);
		} catch (Exception e) {
			System.err.println("Failed to request stop: " + e.getMessage());
			e.printStackTrace();
		}

		runningTasks.remove(id);
	}

	@Override
	public boolean isRunning(Long id) {
		CompletableFuture<?> future = runningTasks.get(id);
		return future != null && !future.isDone();
	}

	private void runJob(ScheduledJobDetail config) {

		String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
		String className = config.getClassName();
		String jobName = className.substring(className.lastIndexOf('.') + 1);
		String logFilePath = String.format(logPath+"%d_%s_%s.log", config.getId(), jobName, timestamp);
		System.out.println("created log file : "+logFilePath);
		try {
			Class<?> clazz = Class.forName(config.getClassName());
			//Object job = clazz.getDeclaredConstructor().newInstance();
			
	        Object job = applicationContext.getBean(clazz); // 🔥 FIX

			if (job instanceof ParameterizedJob) {
				((ParameterizedJob) job).setParameters(config.getJobParams());
				((ParameterizedJob) job).setLogFile(logFilePath);
			}

			if (job instanceof Runnable) {
				((Runnable) job).run();
			}
		} catch (Exception e) {
			System.err.println("Failed to run job: " + e.getMessage());
			e.printStackTrace();
		}
	}

}

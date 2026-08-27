package com.caliper.jobs.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "scheduled_job_detail")
public class ScheduledJobDetail {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(name = "job_name")
	private String jobName;

	@Column(name = "class_name")
    private String className;
	
	@Column(name = "job_params", columnDefinition = "TEXT")
	private String jobParams;
	
	@Column(name = "cron_expression")
    private String cronExpression; // null if one-time
	 
	@Column(name = "one_time")
    private boolean oneTime;
	 
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "next_run")
    private Date nextRun;
	
	@Column(name = "delay_minutes")
	private Integer delayMinutes;
	
	public ScheduledJobDetail() {}

	public ScheduledJobDetail(Long id, String jobName, String className, String jobParams, String cronExpression,
			boolean oneTime, Date nextRun, Integer delayMinutes) {
		super();
		this.id = id;
		this.jobName = jobName;
		this.className = className;
		this.jobParams = jobParams;
		this.cronExpression = cronExpression;
		this.oneTime = oneTime;
		this.nextRun = nextRun;
		this.delayMinutes = delayMinutes;
	}

	public Integer getDelayMinutes() {
		return delayMinutes;
	}

	public void setDelayMinutes(Integer delayMinutes) {
		this.delayMinutes = delayMinutes;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getJobParams() {
		return jobParams;
	}

	public void setJobParams(String jobParams) {
		this.jobParams = jobParams;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public boolean isOneTime() {
		return oneTime;
	}

	public void setOneTime(boolean oneTime) {
		this.oneTime = oneTime;
	}

	public Date getNextRun() {
		return nextRun;
	}

	public void setNextRun(Date nextRun) {
		this.nextRun = nextRun;
	}
}

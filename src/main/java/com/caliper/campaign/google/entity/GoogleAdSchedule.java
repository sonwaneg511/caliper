package com.caliper.campaign.google.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "google_ad_schedule")
@Setter
public class GoogleAdSchedule {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="client_id")
	private String clientId;
	
	@Column(name="day_of_week")
	private String dayOfWeek;
	
	@Column(name="start_hour")
	private int startHour;
	
	@Column(name="end_hour")
	private int endHour;
	
	@Column(name="start_minute")
	private String startMinute;
	
	@Column(name="end_minute")
	private String endMinute;
}

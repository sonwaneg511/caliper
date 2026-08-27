package com.caliper.location.gmb.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.gmb.entity.GMBOperationHours;
import com.caliper.location.gmb.repository.GMBOperationHoursRepository;
import com.google.api.services.mybusinessbusinessinformation.v1.model.BusinessHours;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Location;
import com.google.api.services.mybusinessbusinessinformation.v1.model.TimePeriod;

@Service
public class GMBOperationHoursService {

	@Autowired
	private GMBOperationHoursRepository gmbOperationHoursRepository;
	
	public GMBOperationHours getGMBOperationHours(Location location) {
		BusinessHours regularHours = location.getRegularHours();
		List<TimePeriod> periods = (regularHours != null) ? regularHours.getPeriods() : new ArrayList<TimePeriod>();

		String mondayOpenTime = "";
		String mondayCloseTime = "";
		String tuesdayOpenTime = "";
		String tuesdayCloseTime = "";
		String wednesdayOpenTime = "";
		String wednesdayCloseTime = "";
		String thursdayOpenTime = "";
		String thursdayCloseTime = "";
		String fridayOpenTime = "";
		String fridayCloseTime = "";
		String saturdayOpenTime = "";
		String saturdayCloseTime = "";
		String sundayOpenTime = "";
		String sundayCloseTime = "";


		for(TimePeriod period : periods) {
			String openMinutes = "00";
			String closeMinutes = "00";

			if(period.getOpenTime().getMinutes() !=  null) {
				openMinutes = period.getOpenTime().getMinutes().toString();
			}
			if(period.getCloseTime().getMinutes() !=  null) {
				closeMinutes = period.getCloseTime().getMinutes().toString();
			}

			if(period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_MONDAY)) {
				Integer hours = period.getOpenTime().getHours();
				if(hours != null) {
					mondayOpenTime = period.getOpenTime().getHours().toString()+":"+openMinutes;			
					mondayCloseTime = period.getCloseTime().getHours().toString()+":"+closeMinutes;
				}
			}
			else if(period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_TUESDAY)) {
				Integer hours = period.getOpenTime().getHours();
				if(hours != null) {
					tuesdayOpenTime = period.getOpenTime().getHours().toString()+":"+openMinutes;
					tuesdayCloseTime = period.getCloseTime().getHours().toString()+":"+closeMinutes;
				}
			}
			else if(period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_WEDNESDAY)) {
				Integer hours = period.getOpenTime().getHours();
				if(hours != null) {
					wednesdayOpenTime = period.getOpenTime().getHours().toString()+":"+openMinutes;
					wednesdayCloseTime = period.getCloseTime().getHours().toString()+":"+closeMinutes;
				}
			}
			else if(period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_THURSDAY)) {
				Integer hours = period.getOpenTime().getHours();
				if(hours != null) {
					thursdayOpenTime = period.getOpenTime().getHours().toString()+":"+openMinutes;
					thursdayCloseTime = period.getCloseTime().getHours().toString()+":"+closeMinutes;
				}
			}
			else if(period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_FRIDAY)) {
				Integer hours = period.getOpenTime().getHours();
				if(hours != null) {
					fridayOpenTime = period.getOpenTime().getHours().toString()+":"+openMinutes;
					fridayCloseTime = period.getCloseTime().getHours().toString()+":"+closeMinutes;
				}
			}
			else if(period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_SATURDAY)) {
				Integer hours = period.getOpenTime().getHours();
				if(hours != null) {
					saturdayOpenTime = period.getOpenTime().getHours().toString()+":"+openMinutes;
					saturdayCloseTime = period.getCloseTime().getHours().toString()+":"+closeMinutes;
				}
			}
			else if(period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_SUNDAY)) {
				Integer hours = period.getOpenTime().getHours();
				if(hours != null) {
					sundayOpenTime = period.getOpenTime().getHours().toString()+":"+openMinutes;
					sundayCloseTime = period.getCloseTime().getHours().toString()+":"+closeMinutes;
				}
			}

		}
		GMBOperationHours operationHours = new GMBOperationHours(0L, "", "", "", mondayOpenTime, mondayCloseTime, tuesdayOpenTime, tuesdayCloseTime, wednesdayOpenTime, wednesdayCloseTime,
				thursdayOpenTime, thursdayCloseTime, fridayOpenTime, fridayCloseTime, saturdayOpenTime, saturdayCloseTime, sundayOpenTime, sundayCloseTime, null);

		return operationHours;
	}
	
	public void insertGMBOperationHours(
	        String clientId,
	        String gmbLocationId,
	        GMBOperationHours hours) {
	    
	    GMBOperationHours entity = new GMBOperationHours();

	    entity.setClientId(clientId);
	    entity.setGmbLocationId(gmbLocationId);

	    entity.setMondayOpenTime(hours.getMondayOpenTime());
	    entity.setMondayCloseTime(hours.getMondayCloseTime());
	    entity.setTuesdayOpenTime(hours.getTuesdayOpenTime());
	    entity.setTuesdayCloseTime(hours.getTuesdayCloseTime());
	    entity.setWednesdayOpenTime(hours.getWednesdayOpenTime());
	    entity.setWednesdayCloseTime(hours.getWednesdayCloseTime());
	    entity.setThursdayOpenTime(hours.getThursdayOpenTime());
	    entity.setThursdayCloseTime(hours.getThursdayCloseTime());
	    entity.setFridayOpenTime(hours.getFridayOpenTime());
	    entity.setFridayCloseTime(hours.getFridayCloseTime());
	    entity.setSaturdayOpenTime(hours.getSaturdayOpenTime());
	    entity.setSaturdayCloseTime(hours.getSaturdayCloseTime());
	    entity.setSundayOpenTime(hours.getSundayOpenTime());
	    entity.setSundayCloseTime(hours.getSundayCloseTime());

	    gmbOperationHoursRepository.save(entity);
	}

}

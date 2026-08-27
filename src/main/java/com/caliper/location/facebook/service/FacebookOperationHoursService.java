package com.caliper.location.facebook.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.facebook.entity.FacebookOperationHours;
import com.caliper.location.facebook.repository.FacebookOperationHoursRepository;
import com.restfb.types.Hours;
import com.restfb.types.Hours.DayOfWeek;
import com.restfb.types.Page;
import com.restfb.types.Hours.Hour;

@Service
public class FacebookOperationHoursService {

	@Autowired
	private FacebookOperationHoursRepository facebookOperationHoursRepository;
	
	public List<FacebookOperationHours> getAllFacebookOperationHours(String clientId){
		return facebookOperationHoursRepository.findByClientId(clientId);
		
	}
	
	public Optional<FacebookOperationHours> getFacebookOperationHoursByPageId(String clientId, String fbPageId) {
		return facebookOperationHoursRepository.findByClientIdAndFbPageId(clientId, fbPageId);
	}
	
	public FacebookOperationHours getFacebookOperationHours(Page childPage) {
		Hours hours = childPage.getHours();
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
		
		if(hours != null) {
			Map<DayOfWeek, Map<Integer, Hour>> hoursMap = hours.getHours();
			
			Map<Integer, Hour> dayHoursMap = (hoursMap == null ? null : hoursMap.get(DayOfWeek.SUN));
			if(dayHoursMap != null) {
				sundayOpenTime = dayHoursMap.get(1).getOpen();
				sundayCloseTime = dayHoursMap.get(1).getClose();
			}
			
			dayHoursMap = (hoursMap == null ? null : hoursMap.get(DayOfWeek.MON));
			if(dayHoursMap != null) {
				mondayOpenTime = dayHoursMap.get(1).getOpen();
				mondayCloseTime = dayHoursMap.get(1).getClose();
			}
			
			dayHoursMap = (hoursMap == null ? null : hoursMap.get(DayOfWeek.TUE));
			if(dayHoursMap != null) {
				tuesdayOpenTime = dayHoursMap.get(1).getOpen();
				tuesdayCloseTime = dayHoursMap.get(1).getClose();
			}
			
			dayHoursMap = (hoursMap == null ? null : hoursMap.get(DayOfWeek.WED));
			if(dayHoursMap != null) {
				wednesdayOpenTime = dayHoursMap.get(1).getOpen();
				wednesdayCloseTime = dayHoursMap.get(1).getClose();
			}
			
			dayHoursMap = (hoursMap == null ? null : hoursMap.get(DayOfWeek.THU));
			if(dayHoursMap != null) {
				thursdayOpenTime = dayHoursMap.get(1).getOpen();
				thursdayCloseTime = dayHoursMap.get(1).getClose();
			}
			
		    dayHoursMap = (hoursMap == null ? null : hoursMap.get(DayOfWeek.FRI));
			if(dayHoursMap != null) {
				fridayOpenTime = dayHoursMap.get(1).getOpen();
				fridayCloseTime = dayHoursMap.get(1).getClose();
			}
			
			dayHoursMap = (hoursMap == null ? null : hoursMap.get(DayOfWeek.SAT));
			if(dayHoursMap != null) {
				saturdayOpenTime = dayHoursMap.get(1).getOpen();
				saturdayCloseTime = dayHoursMap.get(1).getClose();
			}
			
			
	
		}
			FacebookOperationHours operationHours = new FacebookOperationHours(0L,"","","", mondayOpenTime, mondayCloseTime, tuesdayOpenTime, tuesdayCloseTime, wednesdayOpenTime, wednesdayCloseTime, thursdayOpenTime, thursdayCloseTime, fridayOpenTime, fridayCloseTime, saturdayOpenTime, saturdayCloseTime, sundayOpenTime, sundayCloseTime, null);
		
			return operationHours;
	}

	public void save(FacebookOperationHours existingHours) {
		facebookOperationHoursRepository.save(existingHours);
		
	}
}

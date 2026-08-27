package com.caliper.location.gmb.dto;

import java.util.List;

public class HoursOfOperationRequest {

	public List<GMBDay> days = null;

	public static class GMBDay {
		public String openDay;
		public String openTime;
		public String closeDay;
		public String closeTime;
		
	}
}

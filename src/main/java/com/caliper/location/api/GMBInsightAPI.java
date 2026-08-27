package com.caliper.location.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.api.services.businessprofileperformance.v1.BusinessProfilePerformance;
import com.google.api.services.businessprofileperformance.v1.model.DailyMetricTimeSeries;
import com.google.api.services.businessprofileperformance.v1.model.FetchMultiDailyMetricsTimeSeriesResponse;

@Component
public class GMBInsightAPI {
	private	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public List<DailyMetricTimeSeries> getDailyMetrics(BusinessProfilePerformance business, List<String> dailyMetricsList, String locationId, Date startDate, Date endDate) throws IOException {		
		String[] split = locationId.split("/");
		String locationID = split[2]+"/"+split[3];
		LocalDate startLocalDate = LocalDate.parse(inputFormat.format(startDate));

		int startDay = startLocalDate.getDayOfMonth();
		Month startMonth = startLocalDate.getMonth();
		int startYear = startLocalDate.getYear();	

		LocalDate endLocalDate = LocalDate.parse(inputFormat.format(endDate));

		int endDay = endLocalDate.getDayOfMonth();
		Month endMonth = endLocalDate.getMonth();
		int endYear = endLocalDate.getYear();	

		try {

			System.out.println("Daily Metrics : "+dailyMetricsList);


			FetchMultiDailyMetricsTimeSeriesResponse execute = business.locations().fetchMultiDailyMetricsTimeSeries(locationID)
					.setDailyMetrics(dailyMetricsList)
					.setDailyRangeStartDateYear(startYear) 
					.setDailyRangeStartDateMonth(startMonth.getValue()) 
					.setDailyRangeStartDateDay(startDay)
					.setDailyRangeEndDateYear(endYear)
					.setDailyRangeEndDateMonth(endMonth.getValue())
					.setDailyRangeEndDateDay(endDay)
					.execute();
			List<DailyMetricTimeSeries> multiDailyMetricTimeSeries = execute.getMultiDailyMetricTimeSeries().get(0).getDailyMetricTimeSeries();
			return multiDailyMetricTimeSeries;
			
		} catch (Exception e) {
			System.out.println("Exception Occured : "+ e );
			System.out.println("Exception = "+ e.getMessage());
			e.printStackTrace();

		}
		return null;		
	}

}

package com.caliper.location.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "dealer_operation_hours")
public class DealerOperationHours {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@JsonIgnore
	private Long id;
	@JsonIgnore
	private String clientId;
	@JsonIgnore
	private String dealerId;

	private String mondayOpenTime;

	private String mondayCloseTime;

	private String tuesdayOpenTime;

	private String tuesdayCloseTime;

	private String wednesdayOpenTime;

	private String wednesdayCloseTime;

	private String thursdayOpenTime;

	private String thursdayCloseTime;

	private String fridayOpenTime;

	private String fridayCloseTime;

	private String saturdayOpenTime;

	private String saturdayCloseTime;

	private String sundayOpenTime;	

	private String sundayCloseTime;
	
//	@OneToOne(mappedBy = "dealerOperationHours")
//	private DealerLocation dealerLocation;

}

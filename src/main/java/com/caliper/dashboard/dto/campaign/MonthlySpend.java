package com.caliper.dashboard.dto.campaign;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySpend {
	private String month;
	private BigDecimal value;
}

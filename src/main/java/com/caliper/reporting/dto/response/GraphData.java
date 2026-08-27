package com.caliper.reporting.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class GraphData {

    private List<ChartData> chartData;

    private List<PieData> pieData;
}

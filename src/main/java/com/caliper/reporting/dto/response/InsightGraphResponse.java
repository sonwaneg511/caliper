package com.caliper.reporting.dto.response;

import lombok.Data;

@Data
public class InsightGraphResponse {

    private GraphData citywiseViews;

    private GraphData statewiseViews;

    private GraphData citywiseActions;

    private GraphData statewiseActions;
}

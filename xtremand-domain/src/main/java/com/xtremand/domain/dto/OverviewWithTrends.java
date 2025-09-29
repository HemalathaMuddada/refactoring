package com.xtremand.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OverviewWithTrends {
	 private Overview overview;
	    private OverviewTrend trend;
}

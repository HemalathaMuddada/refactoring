package com.xtremand.domain.dto;

import lombok.Data;

@Data
public  class DevicePerformance {
    private PerformanceMetrics mobile;
    private PerformanceMetrics desktop;
}
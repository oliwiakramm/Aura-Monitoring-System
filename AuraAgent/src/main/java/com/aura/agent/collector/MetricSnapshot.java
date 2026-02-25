package com.aura.agent.collector;

public record MetricSnapshot(double cpuUsage,long ramUsedMb,long ramMaxMb) {
    public double ramUsagePercent(){
        if(ramMaxMb <= 0) return 0.0;
        return (double) ramUsedMb / ramMaxMb * 100.0;
    }
}

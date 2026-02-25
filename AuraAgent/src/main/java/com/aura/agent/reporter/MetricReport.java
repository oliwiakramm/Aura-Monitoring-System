package com.aura.agent.reporter;

public class MetricReport {
    private final String agentId;
    private final double cpuUsage;
    private final long ramUsageMb;
    private final String healthCheckStatus;
    private final String monitoredUrl;


    public MetricReport(String agentId, double cpuUsage, long ramUsageMb, String healthCheckStatus, String monitoredUrl) {
        this.agentId = agentId;
        this.cpuUsage = cpuUsage;
        this.ramUsageMb = ramUsageMb;
        this.healthCheckStatus = healthCheckStatus;
        this.monitoredUrl = monitoredUrl;
    }


    public String getAgentId() {
        return agentId;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public long getRamUsageMb() {
        return ramUsageMb;
    }

    public String getHealthCheckStatus() {
        return healthCheckStatus;
    }

    public String getMonitoredUrl() {
        return monitoredUrl;
    }
}

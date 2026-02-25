package com.Oliwia.aura.dto;

import jakarta.validation.constraints.*;

public class MetricReportRequest {
    @NotBlank(message = "agentId cannot be blank")
    private String agentId;

    @DecimalMin(value = "0.0", message = "cpuUsage cannot be less than 0")
    @DecimalMax(value="1.0", message = "cpuUsage cannot be over 1.0 (100%)")
    private double cpuUsage;

    @Min(value = 0, message = "ramUsageMb cannot be less than 0")
    private long ramUsageMb;

    @NotBlank(message = "healthCheckStatus cannot be blank")
    @Pattern(regexp = "HEALTHY|UNHEALTHY",message = "healthCheckStatus must be HEALTHY or UNHEALTHY")
    private String healthCheckStatus;

    @NotBlank(message = "monitoredUrl cannot be blank")
    private String monitoredUrl;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public long getRamUsageMb() {
        return ramUsageMb;
    }

    public void setRamUsageMb(long ramUsageMb) {
        this.ramUsageMb = ramUsageMb;
    }

    public String getHealthCheckStatus() {
        return healthCheckStatus;
    }

    public void setHealthCheckStatus(String healthCheckStatus) {
        this.healthCheckStatus = healthCheckStatus;
    }

    public String getMonitoredUrl() {
        return monitoredUrl;
    }

    public void setMonitoredUrl(String monitoredUrl) {
        this.monitoredUrl = monitoredUrl;
    }
}

package com.Oliwia.aura.dto;

import java.time.Instant;

public class AgentStatusResponse {
    private String agentId;
    private String status;
    private double cpuUsage;
    private long ramUsageMb;
    private String monitoredUrl;
    private Instant lastSeenAt;

    private boolean offline;

    public AgentStatusResponse(String agentId, String status, double cpuUsage, long ramUsageMb, String monitoredUrl, Instant lastSeenAt, boolean offline) {
        this.agentId = agentId;
        this.status = status;
        this.cpuUsage = cpuUsage;
        this.ramUsageMb = ramUsageMb;
        this.monitoredUrl = monitoredUrl;
        this.lastSeenAt = lastSeenAt;
        this.offline = offline;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getStatus() {
        return status;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public long getRamUsageMb() {
        return ramUsageMb;
    }

    public String getMonitoredUrl() {
        return monitoredUrl;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public boolean isOffline() {
        return offline;
    }
}

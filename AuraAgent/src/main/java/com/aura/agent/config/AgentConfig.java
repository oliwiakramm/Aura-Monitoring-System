package com.aura.agent.config;

public class AgentConfig {
    private String agentId;
    private String hubUrl;
    private String monitoredUrl;
    private int intervalSeconds;
    private int httpTimeoutSeconds;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public void setHubUrl(String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public String getMonitoredUrl() {
        return monitoredUrl;
    }

    public void setMonitoredUrl(String monitoredUrl) {
        this.monitoredUrl = monitoredUrl;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public int getHttpTimeoutSeconds() {
        return httpTimeoutSeconds;
    }

    public void setHttpTimeoutSeconds(int httpTimeoutSeconds) {
        this.httpTimeoutSeconds = httpTimeoutSeconds;
    }

    @Override
    public String toString(){
        return String.format(
                "AgentConfig{agentId='%s',hubUrl='%s', monitoredUrl='%s', intervalSeconds=%d,httpTimeoutSeconds=%d}",
                agentId,hubUrl,monitoredUrl,intervalSeconds,httpTimeoutSeconds
        );
    }
}

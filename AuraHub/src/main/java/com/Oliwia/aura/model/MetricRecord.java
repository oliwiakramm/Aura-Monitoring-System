package com.Oliwia.aura.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "metric_records",indexes={
        @Index(name="idx_agent_id",columnList = "agentId"),
        @Index(name="idx_timestamp",columnList = "timestamp"),
})

public class MetricRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String agentId;

    @Column(nullable = false)
    private double cpuUsage;

    @Column(nullable = false)
    private long ramUsageMb;

    @Column(nullable = false)
    private String healthCheckStatus;

    @Column(nullable = false)
    private String monitoredUrl;

    @Column(nullable = false)
    private Instant timestamp;


    public Long getId() {
        return id;
    }


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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}

package com.aura.agent.scheduler;

import com.aura.agent.collector.JvmMetricCollector;
import com.aura.agent.collector.MetricSnapshot;
import com.aura.agent.config.AgentConfig;
import com.aura.agent.healthcheck.HealthCheckResult;
import com.aura.agent.healthcheck.UrlHealthChecker;
import com.aura.agent.reporter.HubReporter;
import com.aura.agent.reporter.MetricReport;

import java.util.logging.Logger;

public class MonitoringTask implements Runnable {

    private static final Logger LOG = Logger.getLogger(MonitoringTask.class.getName());

    private final AgentConfig config;
    private final JvmMetricCollector metricCollector;
    private final UrlHealthChecker healthChecker;
    private final HubReporter hubReporter;

    public MonitoringTask(AgentConfig config, JvmMetricCollector metricCollector, UrlHealthChecker healthChecker, HubReporter hubReporter) {
        this.config = config;
        this.metricCollector = metricCollector;
        this.healthChecker = healthChecker;
        this.hubReporter = hubReporter;
    }


    @Override
    public void run(){
        try{
            LOG.info("-- Monitoring Cycle --");

            MetricSnapshot metrics = metricCollector.collect();

            HealthCheckResult healthResult = healthChecker.check(config.getMonitoredUrl());

            MetricReport report = new MetricReport(
                    config.getAgentId(),
                    metrics.cpuUsage(),
                    metrics.ramUsedMb(),
                    healthResult.status(),
                    config.getMonitoredUrl()
            );

            hubReporter.send(report);
        } catch(Exception e){
            LOG.severe("Unexpected error in monitoring cycle: " + e.getMessage());
        }
    }
}

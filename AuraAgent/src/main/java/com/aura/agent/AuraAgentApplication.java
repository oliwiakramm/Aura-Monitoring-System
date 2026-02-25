package com.aura.agent;

import com.aura.agent.collector.JvmMetricCollector;
import com.aura.agent.config.AgentConfig;
import com.aura.agent.config.ConfigLoader;
import com.aura.agent.healthcheck.UrlHealthChecker;
import com.aura.agent.reporter.HubReporter;
import com.aura.agent.scheduler.AgentScheduler;
import com.aura.agent.scheduler.MonitoringTask;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AuraAgentApplication {
    private static  final Logger LOG = Logger.getLogger(AuraAgentApplication.class.getName());

    public static void main(String[] args) {
        configureLogging();

        AgentConfig config = new ConfigLoader().load();
        LOG.info("Configuration loaded: " + config);

        JvmMetricCollector collector = new JvmMetricCollector();
        UrlHealthChecker checker = new UrlHealthChecker(config.getHttpTimeoutSeconds());
        HubReporter reporter = new HubReporter(config.getHubUrl(),config.getHttpTimeoutSeconds());
        MonitoringTask task = new MonitoringTask(config,collector,checker,reporter);
        AgentScheduler scheduler = new AgentScheduler(config,task);

        LOG.info(String.format("Aura agent [%s] ready to work.",config.getAgentId()));
        scheduler.start();
    }

    private static void configureLogging() {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tH:%1$tM:%1$tS | %4$-7s | %2$s — %5$s%6$s%n");

        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);

        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(handler);
    }

}

package com.aura.agent.scheduler;

import com.aura.agent.config.AgentConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AgentScheduler {

    private static final Logger LOG = Logger.getLogger(AgentScheduler.class.getName());

    private final ScheduledExecutorService scheduler;
    private final MonitoringTask task;
    private final int intervalSeconds;

    public AgentScheduler(AgentConfig config, MonitoringTask task){
        this.task = task;
        this.intervalSeconds = config.getIntervalSeconds();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable,"aura-agent-worker");
            thread.setDaemon(false);
            return thread;
        });
    }

    public void start(){
        LOG.info(String.format("Scheduler running. Reports are sent once in %d seconds",intervalSeconds));

        scheduler.scheduleAtFixedRate(task,0,intervalSeconds, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop,"aura-shutdown-hook"));
    }

    public void stop(){
        LOG.info("Stopping Agent...");
        scheduler.shutdown();
        try{
            if(!scheduler.awaitTermination(5,TimeUnit.SECONDS)){
                scheduler.shutdownNow();
                LOG.warning("Agent stopped after timeout.");
            }else{
                LOG.info("Agent has stopped.");
            }
        }catch(InterruptedException e){
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

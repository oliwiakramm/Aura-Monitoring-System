package com.Oliwia.aura.service;

import com.Oliwia.aura.dto.AgentStatusResponse;
import com.Oliwia.aura.dto.MetricReportRequest;
import com.Oliwia.aura.model.MetricRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Oliwia.aura.repository.MetricRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class MetricService {
    private final MetricRepository metricRepository;

    @Value("${aura.agent.timeout-seconds}")
    private long agentTimeoutSeconds;

    @Value("${aura.thresholds.cpu-critical}")
    private double cpuCriticalThreshold;

    @Value("${aura.thresholds.ram-critical-mb}")
    private long ramCriticalThresholdMb;

    public MetricService(MetricRepository metricRepository){
        this.metricRepository = metricRepository;
    }

   @Transactional
    public MetricRecord saveReport(MetricReportRequest request){
        MetricRecord record = new MetricRecord();
        record.setAgentId(request.getAgentId());
        record.setCpuUsage(request.getCpuUsage());
        record.setRamUsageMb(request.getRamUsageMb());
        record.setHealthCheckStatus(request.getHealthCheckStatus());
        record.setMonitoredUrl(request.getMonitoredUrl());
        record.setTimestamp(Instant.now());

        return metricRepository.save(record);
   }

   @Transactional(readOnly = true)
    public List<AgentStatusResponse> getAllAgentStatuses(){
       List<String> agentIds = metricRepository.findAllDistinctAgentIds();

       return agentIds.stream().map(this::buildAgentStatus).toList();
   }


   private AgentStatusResponse buildAgentStatus(String agentId){
       Optional<MetricRecord> lastestOpt = metricRepository.findTopByAgentIdOrderByTimestampDesc(agentId);

       if(lastestOpt.isEmpty()){
           return new AgentStatusResponse(agentId,"UNKNOWN",0,0,"N/A",Instant.now(),true);
       }

       MetricRecord latest = lastestOpt.get();
       boolean offline = isOffline(latest.getTimestamp());
       String status = offline? "OFFLINE": computeStatus(latest);

       return new AgentStatusResponse(
               agentId,
               status,
               latest.getCpuUsage(),
               latest.getRamUsageMb(),
               latest.getMonitoredUrl(),
               latest.getTimestamp(),
               offline
       );
   }

   private boolean isOffline(Instant lastSeen){
        return lastSeen.isBefore(Instant.now().minus(agentTimeoutSeconds, ChronoUnit.SECONDS));
   }

   private String computeStatus(MetricRecord record){
        boolean highCpu = record.getCpuUsage() > cpuCriticalThreshold;
        boolean highRam = record.getRamUsageMb() > ramCriticalThresholdMb;
        boolean unhealthy = "UNHEALTHY".equals(record.getHealthCheckStatus());

        if(highCpu || highRam || unhealthy){
            return "CRITICAL";
        }

        return "HEALTHY";

   }
}

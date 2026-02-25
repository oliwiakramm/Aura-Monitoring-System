package com.Aura;

import com.Oliwia.aura.dto.AgentStatusResponse;
import com.Oliwia.aura.dto.MetricReportRequest;
import com.Oliwia.aura.model.MetricRecord;
import com.Oliwia.aura.repository.MetricRepository;
import com.Oliwia.aura.service.MetricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    @Mock
    private MetricRepository metricRepository;

    @InjectMocks
    private MetricService metricService;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(metricService, "agentTimeoutSeconds", 30L);
        ReflectionTestUtils.setField(metricService, "cpuCriticalThreshold", 0.85);
        ReflectionTestUtils.setField(metricService, "ramCriticalThresholdMb", 512L);
    }



    @Test
    @DisplayName("saveReport: powinien zapisać raport i zwrócić encję z timestampem")
    void saveReport_shouldPersistRecordWithTimestamp() {

        MetricReportRequest request = buildRequest("agent-1", 0.5, 256, "HEALTHY");
        MetricRecord savedRecord = buildRecord("agent-1", 0.5, 256, "HEALTHY", Instant.now());
        when(metricRepository.save(any(MetricRecord.class))).thenReturn(savedRecord);


        MetricRecord result = metricService.saveReport(request);


        assertThat(result.getAgentId()).isEqualTo("agent-1");
        assertThat(result.getCpuUsage()).isEqualTo(0.5);
        verify(metricRepository, times(1)).save(any(MetricRecord.class));
    }



    @Test
    @DisplayName("getAllAgentStatuses: agent z normalnym CPU/RAM = HEALTHY")
    void getStatus_normalMetrics_shouldReturnHealthy() {

        MetricRecord record = buildRecord("agent-1", 0.4, 200, "HEALTHY", Instant.now());
        when(metricRepository.findAllDistinctAgentIds()).thenReturn(List.of("agent-1"));
        when(metricRepository.findTopByAgentIdOrderByTimestampDesc("agent-1"))
                .thenReturn(Optional.of(record));


        List<AgentStatusResponse> statuses = metricService.getAllAgentStatuses();


        assertThat(statuses).hasSize(1);
        assertThat(statuses.get(0).getStatus()).isEqualTo("HEALTHY");
        assertThat(statuses.get(0).isOffline()).isFalse();
    }

    @Test
    @DisplayName("getAllAgentStatuses: CPU > 85% = CRITICAL")
    void getStatus_highCpu_shouldReturnCritical() {

        MetricRecord record = buildRecord("agent-1", 0.92, 200, "HEALTHY", Instant.now());
        when(metricRepository.findAllDistinctAgentIds()).thenReturn(List.of("agent-1"));
        when(metricRepository.findTopByAgentIdOrderByTimestampDesc("agent-1"))
                .thenReturn(Optional.of(record));


        List<AgentStatusResponse> statuses = metricService.getAllAgentStatuses();


        assertThat(statuses.get(0).getStatus()).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("getAllAgentStatuses: RAM > 512MB = CRITICAL")
    void getStatus_highRam_shouldReturnCritical() {

        MetricRecord record = buildRecord("agent-1", 0.3, 600, "HEALTHY", Instant.now());
        when(metricRepository.findAllDistinctAgentIds()).thenReturn(List.of("agent-1"));
        when(metricRepository.findTopByAgentIdOrderByTimestampDesc("agent-1"))
                .thenReturn(Optional.of(record));


        List<AgentStatusResponse> statuses = metricService.getAllAgentStatuses();


        assertThat(statuses.get(0).getStatus()).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("getAllAgentStatuses: healthCheck = UNHEALTHY = CRITICAL")
    void getStatus_failedHealthCheck_shouldReturnCritical() {

        MetricRecord record = buildRecord("agent-1", 0.3, 100, "UNHEALTHY", Instant.now());
        when(metricRepository.findAllDistinctAgentIds()).thenReturn(List.of("agent-1"));
        when(metricRepository.findTopByAgentIdOrderByTimestampDesc("agent-1"))
                .thenReturn(Optional.of(record));


        List<AgentStatusResponse> statuses = metricService.getAllAgentStatuses();


        assertThat(statuses.get(0).getStatus()).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("getAllAgentStatuses: ostatni raport > 30s temu = OFFLINE")
    void getStatus_agentSilentTooLong_shouldReturnOffline() {

        Instant longAgo = Instant.now().minus(60, ChronoUnit.SECONDS);
        MetricRecord record = buildRecord("agent-1", 0.3, 100, "HEALTHY", longAgo);
        when(metricRepository.findAllDistinctAgentIds()).thenReturn(List.of("agent-1"));
        when(metricRepository.findTopByAgentIdOrderByTimestampDesc("agent-1"))
                .thenReturn(Optional.of(record));


        List<AgentStatusResponse> statuses = metricService.getAllAgentStatuses();


        assertThat(statuses.get(0).isOffline()).isTrue();
        assertThat(statuses.get(0).getStatus()).isEqualTo("OFFLINE");
    }



    private MetricReportRequest buildRequest(String agentId, double cpu, long ram, String health) {
        MetricReportRequest req = new MetricReportRequest();
        req.setAgentId(agentId);
        req.setCpuUsage(cpu);
        req.setRamUsageMb(ram);
        req.setHealthCheckStatus(health);
        req.setMonitoredUrl("https://google.com");
        return req;
    }

    private MetricRecord buildRecord(String agentId, double cpu, long ram, String health, Instant ts) {
        MetricRecord r = new MetricRecord();
        r.setAgentId(agentId);
        r.setCpuUsage(cpu);
        r.setRamUsageMb(ram);
        r.setHealthCheckStatus(health);
        r.setMonitoredUrl("https://google.com");
        r.setTimestamp(ts);
        return r;
    }
}


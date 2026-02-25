package com.Oliwia.aura.controller;

import com.Oliwia.aura.dto.AgentStatusResponse;
import com.Oliwia.aura.dto.MetricReportRequest;
import jakarta.validation.Valid;
import com.Oliwia.aura.model.MetricRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.Oliwia.aura.service.MetricService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MetricController {
    private final MetricService metricService;

    public MetricController(MetricService metricService){
        this.metricService = metricService;
    }

    @PostMapping("/metrics")
    public ResponseEntity<Map<String,Object>> receiveMetrics(@Valid @RequestBody MetricReportRequest request){
        MetricRecord saved = metricService.saveReport(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Report saved","recordId",saved.getId(),"agentId",saved.getAgentId()));
    }


    @GetMapping("/status")
    public ResponseEntity<List<AgentStatusResponse>> getStatus(){
        List<AgentStatusResponse> statuses = metricService.getAllAgentStatuses();
        return ResponseEntity.ok(statuses);
    }
}

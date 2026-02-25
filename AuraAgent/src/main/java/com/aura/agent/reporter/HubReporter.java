package com.aura.agent.reporter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HubReporter {
    private static final Logger LOG = Logger.getLogger(HubReporter.class.getName());

    private final String hubUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HubReporter(String hubUrl, int timeoutSeconds){
        this.hubUrl = hubUrl;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeoutSeconds)).build();
    }


    public void send(MetricReport report){
        try{
            String json = objectMapper.writeValueAsString(report);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hubUrl)).timeout(Duration.ofSeconds(10)).header("Content-Type","application/json")
                    .header("User-Agent","AuraAgent/1.0").POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

            if(isSuccess(response.statusCode())){
                LOG.info(String.format("[%s] V Report sent to Hub | CPU=%.1f%% RAM=%dMB Health=%s",report.getAgentId(),
                        report.getCpuUsage() * 100,report.getRamUsageMb(),report.getHealthCheckStatus()));
            }else{
                LOG.warning(String.format("[%s] X Hub rejected report | HTTP %d | Response: %s",report.getAgentId(),
                        response.statusCode(),response.body()));
            }
        } catch(java.net.http.HttpTimeoutException e){
            LOG.warning("[" + report.getAgentId() + "] X Timeout while sending to Hub: " + hubUrl);
        } catch (java.net.ConnectException e){
            LOG.warning("[" + report.getAgentId() + "] X No connection to Hub: " + hubUrl + " - check whether Hub is switched on");
        } catch(Exception e){
            LOG.log(Level.SEVERE,"[" + report.getAgentId() + "] X Unexpected error occurred while sending report", e);
        }
    }

    private boolean isSuccess(int statusCode){
        return statusCode >= 200 && statusCode < 300;
    }
}

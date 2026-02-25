package com.aura.agent.healthcheck;

public record HealthCheckResult(String status,int httpCode,long responseMs,String errorMessage) {
    public boolean isHealthy(){
        return "HEALTHY".equals(status);
    }

    public static HealthCheckResult healthy(int httpCode,long responseMs){
        return new HealthCheckResult("HEALTHY",httpCode,responseMs,null);
    }

    public static HealthCheckResult unhealthy(int httpCode,long responseMs,String reason){
        return new HealthCheckResult("UNHEALTHY",httpCode,responseMs,reason);
    }

}

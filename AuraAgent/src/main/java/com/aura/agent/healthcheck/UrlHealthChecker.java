package com.aura.agent.healthcheck;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UrlHealthChecker {
    private static final Logger LOG = Logger.getLogger(UrlHealthChecker.class.getName());

    private final HttpClient httpClient;
    private final Duration timeout;

    public UrlHealthChecker(int timeoutSeconds) {
       this.timeout = Duration.ofSeconds(timeoutSeconds);
       this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).followRedirects(HttpClient.Redirect.NORMAL).build();
    }

    public HealthCheckResult check(String url){
        LOG.info("Health check: " + url);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(timeout).GET()
                .header("User-Agent","AuraAgent/1.0").build();

        Instant start = Instant.now();

        try{
            HttpResponse<Void> response = httpClient.send(request,HttpResponse.BodyHandlers.discarding());
            long responseMs = Duration.between(start,Instant.now()).toMillis();
            int code = response.statusCode();

            if(isSuccessCode(code)){
                LOG.info(String.format("Health check OK: HTTP %d in %dms",code,responseMs));
                return HealthCheckResult.healthy(code,responseMs);
            }else{
                LOG.warning(String.format("Health check FAILED: HTTP %d in %dms",code,responseMs));
                return HealthCheckResult.unhealthy(code,responseMs,"HTTP " + code);
            }
        }catch (java.net.http.HttpTimeoutException e){
            long responseMs = Duration.between(start,Instant.now()).toMillis();
            LOG.warning("Health check TIMEOUT after " + responseMs + "ms for: " + url);
            return HealthCheckResult.unhealthy(0,responseMs,"Timeout after "+ responseMs + "ms");
        } catch(Exception e){
            long responseMs = Duration.between(start,Instant.now()).toMillis();
            LOG.log(Level.WARNING,"Health check ERROE for: "+ url,e);
            return HealthCheckResult.unhealthy(0,responseMs,"Error: " + e.getMessage());
        }
    }

    private boolean isSuccessCode(int code){
        return code >= 200 && code < 300;
    }
}

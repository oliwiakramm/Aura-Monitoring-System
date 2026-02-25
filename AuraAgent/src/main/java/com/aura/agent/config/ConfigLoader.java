package com.aura.agent.config;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ConfigLoader {

    private static final Logger LOG = Logger.getLogger(ConfigLoader.class.getName());
    private static final String CONFIG_FILE_NAME = "config.json";

    private final ObjectMapper objectMapper = new ObjectMapper();


    public AgentConfig load() {

        File externalConfig = new File(CONFIG_FILE_NAME);
        if (externalConfig.exists()) {
            LOG.info("Loading configuration form exiternal file: " + externalConfig.getAbsolutePath());
            return parseAndValidate(externalConfig);
        }


        var classpathResource = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
        if (classpathResource != null) {
            LOG.info("Loading configuration form classpath");
            try {
                return validateConfig(objectMapper.readValue(classpathResource, AgentConfig.class));
            } catch (IOException e) {
                throw new ConfigurationException("Parse error while loading config from classpath: " + e.getMessage(), e);
            }
        }

        throw new ConfigurationException(
                "Could not find config.json. " +
                        "Put it into a folder: " + new File(".").getAbsolutePath()
        );
    }



    private AgentConfig parseAndValidate(File file) {
        try {
            return validateConfig(objectMapper.readValue(file, AgentConfig.class));
        } catch (IOException e) {
            throw new ConfigurationException("Parse error " + file.getPath() + ": " + e.getMessage(), e);
        }
    }

    private AgentConfig validateConfig(AgentConfig config) {
        if (config.getAgentId() == null || config.getAgentId().isBlank()) {
            throw new ConfigurationException("'agentId' field is required in config.json");
        }
        if (config.getHubUrl() == null || config.getHubUrl().isBlank()) {
            throw new ConfigurationException("'hubUrl' field is required in config.json");
        }
        if (config.getMonitoredUrl() == null || config.getMonitoredUrl().isBlank()) {
            throw new ConfigurationException("'monitoredUrl' field is required in config.json");
        }
        if (config.getIntervalSeconds() <= 0) {
            throw new ConfigurationException("Field 'intervalSeconds' must be greater than 0");
        }
        if (config.getHttpTimeoutSeconds() <= 0) {
            throw new ConfigurationException("Field 'httpTimeoutSeconds' must be greater than 0");
        }
        return config;
    }
}

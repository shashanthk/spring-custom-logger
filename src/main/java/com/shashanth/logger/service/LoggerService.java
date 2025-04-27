package com.shashanth.logger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shashanth.logger.context.RequestContext;
import com.shashanth.logger.model.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class LoggerService {

    private static final Logger logger = LoggerFactory.getLogger(LoggerService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void log(String level, String message, Object data) {
        try {
            LogEvent event = LogEvent.builder()
                    .level(level)
                    .message(message)
                    .requestId(RequestContext.getRequestId())
                    .timestamp(Instant.now().toString())
                    .data(data)
                    .metaData(_dynamicMetadata())
                    .build();

            logger.info(objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            logger.error("Failed to log event", e);
        }
    }

    private Map<String, Object> _dynamicMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("environment", "production"); // You can inject from env
        metadata.put("ipAddress", RequestContext.getIpAddress());
        return metadata;
    }
}


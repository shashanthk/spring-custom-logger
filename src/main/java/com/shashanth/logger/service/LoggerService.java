package com.shashanth.logger.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shashanth.logger.context.RequestContext;
import com.shashanth.logger.model.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class LoggerService {

    private static final Logger logger = LoggerFactory.getLogger(LoggerService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private static final LinkedBlockingQueue<LogEvent> logQueue = new LinkedBlockingQueue<>();

    public LoggerService() {
        executor.submit(this::_consumeLogs);
    }

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

            boolean added = logQueue.offer(event, 2, TimeUnit.SECONDS); // Queue it instead of logging directly!

            if (!added) {
                logger.error("Logger queue full! Dropping log: {}", message);
            }

        } catch (InterruptedException e) {
            logger.error("Failed to log event", e);
        }
    }

    private Map<String, Object> _dynamicMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("environment", "production"); // You can inject from env
        metadata.put("ipAddress", RequestContext.getIpAddress());
        return metadata;
    }

    private void _consumeLogs() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                logger.info(objectMapper.writeValueAsString(logQueue.take()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Logger thread interrupted, stopping log consumption");
                break;
            } catch (JsonProcessingException e) {
                logger.error("Logger failed", e);
            }
        }
    }
}


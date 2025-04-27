package com.shashanth.logger.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogEvent {
    private String level;
    private String message;
    private String requestId;
    private String timestamp;
    private Object data;
}

package com.shashanth.logger.util;

import com.shashanth.logger.service.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LogHelper {

    private static LoggerService loggerService;

    @Autowired
    public LogHelper(LoggerService service) {
        LogHelper.loggerService = service;
    }

    public static void info(String message) {
        loggerService.log("INFO", message, null);
    }

    public static void info(String message, Object data) {
        loggerService.log("INFO", message, data);
    }

    public static void error(String message) {
        loggerService.log("ERROR", message, null);
    }

    public static void error(String message, Object data) {
        loggerService.log("ERROR", message, data);
    }

    public static void warn(String message) {
        loggerService.log("WARN", message, null);
    }

    public static void debug(String message) {
        loggerService.log("DEBUG", message, null);
    }

    public static void audit(String message, Object data) {
        loggerService.log("AUDIT", message, data);
    }
}


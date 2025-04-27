package com.shashanth.logger.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for safely extracting and parsing request and response bodies and parameters.
 */
public class RequestResponseBodyParser {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseBodyParser.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private RequestResponseBodyParser() {
        // Utility class, no instantiation
    }

    /**
     * Parses byte array content into a JSON object if possible.
     * Returns "" for empty bodies, or plain string if not valid JSON.
     */
    public static Object parseBodyAsJson(byte[] content, String encoding) {

        if (content == null || content.length == 0) return "";

        try {
            String body = new String(content, Optional.ofNullable(encoding).orElse(StandardCharsets.UTF_8.name()));
            return (body.isBlank()) ? "" : objectMapper.readValue(body, Object.class);
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            logger.warn("Failed to parse body into JSON", e);
            return "Failed to parse content";
        }
    }

    /**
     * Extracts query parameters into a simple Map.
     * Flattens single-value arrays for cleaner logs.
     */
    public static Map<String, Object> extractRequestParams(HttpServletRequest request) {

        Map<String, String[]> paramMap = request.getParameterMap();
        Map<String, Object> flatParams = new HashMap<>();

        paramMap.forEach((key, value) -> flatParams.put(key, (value != null && value.length == 1) ? value[0] : value));

        return flatParams;
    }
}

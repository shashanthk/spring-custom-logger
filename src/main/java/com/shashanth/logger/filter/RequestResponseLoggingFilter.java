package com.shashanth.logger.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shashanth.logger.context.RequestContext;
import com.shashanth.logger.util.LogHelper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Global filter to capture and log every API request and response
 * in a structured, non-blocking, Loki-friendly format.
 */
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {

        // Wrap request and response for caching bodies
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // Set RequestContext with request ID and IP Address
            String requestId = Optional.ofNullable(wrappedRequest.getHeader("X-Request-ID"))
                    .orElse(UUID.randomUUID().toString());

            RequestContext.setRequestId(requestId);
            RequestContext.setIpAddress(wrappedRequest.getRemoteAddr());

            // Proceed with filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            // After completion of the request, log details
            logRequestAndResponse(wrappedRequest, wrappedResponse);

            // VERY IMPORTANT: copy cached response back to actual response
            wrappedResponse.copyBodyToResponse();

            // Clean up thread-local context
            RequestContext.clear();
        }
    }

    /**
     * Logs the request and response details after request completion.
     */
    private void logRequestAndResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        try {
            Object requestBody = getContentAsJson(request.getContentAsByteArray(), request.getCharacterEncoding());
            Object responseBody = getContentAsJson(response.getContentAsByteArray(), response.getCharacterEncoding());
            Map<String, Object> requestParams = extractRequestParams(request);

            LogHelper.info(
                    "Request/Response Detail",
                    Map.of(
                            "requestUri", request.getRequestURI(),
                            "requestMethod", request.getMethod(),
                            "responseStatus", response.getStatus(),
                            "requestParams", requestParams,
                            "requestBody", requestBody,
                            "responseBody", responseBody
                    )
            );

        } catch (Exception e) {
            logger.error("Failed to log request/response", e);
        }
    }

    /**
     * Parses the given byte content into a JSON object if possible.
     * Falls back gracefully if body is empty or invalid JSON.
     */
    private Object getContentAsJson(byte[] content, String encoding) {

        if (content == null || content.length == 0) {
            return "";
        }

        try {

            String body = new String(content, Optional.ofNullable(encoding).orElse(StandardCharsets.UTF_8.name()));

            if (body.isBlank()) {
                return "";
            }

            // Try to parse as JSON
            return objectMapper.readValue(body, Object.class);
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            logger.warn("Failed to parse request/response body to JSON", e);
            return "Failed to parse content";
        }
    }

    /**
     * Extracts query parameters into a simple Map.
     * Single-value arrays are flattened for cleaner logs.
     */
    private Map<String, Object> extractRequestParams(HttpServletRequest request) {

        Map<String, String[]> paramMap = request.getParameterMap();
        Map<String, Object> flatParams = new HashMap<>();

        paramMap.forEach((key, value) -> flatParams.put(key, (value != null && value.length == 1) ? value[0] : value));

        return flatParams;
    }
}

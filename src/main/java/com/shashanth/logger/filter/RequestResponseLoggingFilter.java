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

public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {

        // Wrap request and response to allow multiple reads
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {

            // Set request ID if not present
            String requestId = wrappedRequest.getHeader("X-Request-ID");
            requestId = Optional.ofNullable(requestId).orElse(UUID.randomUUID().toString());

            RequestContext.setRequestId(requestId);
            RequestContext.setIpAddress(wrappedRequest.getRemoteAddr());

            // Continue the filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            // After completion, log details
            logRequestAndResponse(wrappedRequest, wrappedResponse);

            // VERY IMPORTANT: copy response body back to actual output
            wrappedResponse.copyBodyToResponse();

            // Clean up
            RequestContext.clear();
        }
    }

    private void logRequestAndResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        try {

            Object requestBody = getContentAsString(request.getContentAsByteArray(), request.getCharacterEncoding());
            Object responseBody = getContentAsString(response.getContentAsByteArray(), response.getCharacterEncoding());
            Object requestParams = extractRequestParams(request);

            // Log basic request/response details
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
            logger.error("Fatal Error", e);
        }
    }

    private Object getContentAsString(byte[] content, String encoding) {

        if (content == null || content.length == 0) return "";

        encoding = Optional.ofNullable(encoding).orElse(StandardCharsets.UTF_8.name());

        try {

            String body = new String(content, encoding);

            return (body.isBlank() || body.isEmpty()) ? "{}" : new ObjectMapper().readValue(body, Object.class);
        } catch (UnsupportedEncodingException | JsonProcessingException ex) {
            return "Failed to parse content";
        }
    }

    private Map<String, Object> extractRequestParams(HttpServletRequest request) {

        Map<String, Object> flatParams = new HashMap<>();

        request.getParameterMap().forEach((key, value) -> flatParams.put(key, (value.length == 1) ? value[0] : value));

        return flatParams;
    }

}


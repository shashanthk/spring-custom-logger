package io.github.shashanthk.logger.filter;

import io.github.shashanthk.logger.context.RequestContext;
import io.github.shashanthk.logger.util.LogHelper;
import io.github.shashanthk.logger.util.RequestResponseBodyParser;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Global filter to capture and log every API request and response
 * in a structured, non-blocking, Loki-friendly format.
 */
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {

        // Wrap request and response for caching bodies
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();  // Capture start time

        try {
            // Set RequestContext with request ID and IP Address
            String requestId = Optional.ofNullable(wrappedRequest.getHeader("X-Request-ID"))
                    .orElse(UUID.randomUUID().toString());

            RequestContext.setRequestId(requestId);
            RequestContext.setIpAddress(wrappedRequest.getRemoteAddr());

            // Proceed with filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {

            long endTime = System.currentTimeMillis(); // Capture end time

            // After completion of the request, log details
            logRequestAndResponse(wrappedRequest, wrappedResponse, startTime, endTime);

            // VERY IMPORTANT: copy cached response back to actual response
            wrappedResponse.copyBodyToResponse();

            // Clean up thread-local context
            RequestContext.clear();
        }
    }

    /**
     * Logs the request and response details after request completion.
     */
    private void logRequestAndResponse(
            ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
            long startTime, long endTime
    ) {
        try {

            LogHelper.info(
                    "Request/Response Detail",
                    Map.of(
                            "requestUri", request.getRequestURI(),
                            "requestMethod", request.getMethod(),
                            "responseStatus", response.getStatus(),
                            "startTime", startTime,
                            "endTime", endTime,
                            "durationMs", (endTime - startTime),
                            "requestParams", RequestResponseBodyParser.extractRequestParams(request),
                            "requestBody", RequestResponseBodyParser.parseBodyAsJson(request.getContentAsByteArray(), request.getCharacterEncoding()),
                            "responseBody", RequestResponseBodyParser.parseBodyAsJson(response.getContentAsByteArray(), response.getCharacterEncoding())
                    )
            );

        } catch (Exception e) {
            logger.error("Failed to log request/response", e);
        }
    }
}

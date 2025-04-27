package com.shashanth.logger.filter;

import com.shashanth.logger.context.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate or capture existing Request ID
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        RequestContext.setRequestId(requestId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContext.clear();
    }
}

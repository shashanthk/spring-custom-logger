package io.github.shashanthk.logger.aop;

import io.github.shashanthk.logger.config.LogLevel;
import io.github.shashanthk.logger.context.RequestContext;
import io.github.shashanthk.logger.util.LogHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Aspect for intercepting methods annotated with @Loggable and logging their execution details.
 */
@Aspect
@Component
public class LoggableAspect {

    private static final Logger internalLogger = LoggerFactory.getLogger(LoggableAspect.class);

    @Around("@annotation(loggable)")
    public Object logExecutionDetails(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methodName = methodSignature.getDeclaringType().getSimpleName() + "#" + methodSignature.getMethod().getName();

        Object result = null;
        Exception capturedException = null;

        Map<String, Object> methodLog = prepareInitialLog(methodName, joinPoint.getArgs(), startTime, loggable);

        try {
            result = joinPoint.proceed();
            methodLog.put("executionStatus", "SUCCESS");

            // Log results only if enabled
            if (loggable.logResult()) {
                methodLog.put("returnValue", result);
            }

            return result;

        } catch (Exception e) {
            capturedException = e;
            methodLog.put("executionStatus", "FAILED");
            methodLog.put("exception", e.getMessage());
            throw e;

        } finally {
            long endTime = System.currentTimeMillis();
            methodLog.put("endTime", endTime);
            methodLog.put("durationMs", endTime - startTime);

            LogLevel effectiveLevel = (capturedException == null) ? loggable.level() : LogLevel.ERROR;
            logMethodExecution(effectiveLevel, methodName, methodLog);
        }
    }

    private Map<String, Object> prepareInitialLog(String methodName, Object[] args, long startTime, Loggable loggable) {
        Map<String, Object> methodLog = new HashMap<>();
        methodLog.put("methodName", methodName);

        // Log arguments only if enabled
        if (loggable.logArgs()) {
            methodLog.put("arguments", args);
        }

        methodLog.put("requestId", RequestContext.getRequestId());
        methodLog.put("startTime", startTime);
        return methodLog;
    }

    private void logMethodExecution(LogLevel logLevel, String methodName, Map<String, Object> methodLog) {
        try {
            switch (logLevel) {
                case DEBUG -> LogHelper.debug("Executing %s()".formatted(methodName));
                case WARN -> LogHelper.warn("Executing %s()".formatted(methodName));
                case ERROR -> LogHelper.error("Error during %s()".formatted(methodName), methodLog);
                default -> LogHelper.info("Executing %s()".formatted(methodName), methodLog);
            }
        } catch (Exception loggingException) {
            internalLogger.error("Logging failed inside LoggableAspect for method: {}", methodName, loggingException);
        }
    }
}

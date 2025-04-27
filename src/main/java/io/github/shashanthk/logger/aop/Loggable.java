package io.github.shashanthk.logger.aop;

import io.github.shashanthk.logger.config.LogLevel;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Loggable {

    String value() default "";

    LogLevel level() default LogLevel.INFO;

    boolean logArgs() default true;

    boolean logResult() default true;
}

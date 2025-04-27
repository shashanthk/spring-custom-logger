package com.shashanth.logger.config;

import com.shashanth.logger.filter.LoggingInterceptor;
import com.shashanth.logger.service.LoggerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LoggerAutoConfiguration implements WebMvcConfigurer {

    @Bean
    @ConditionalOnMissingBean
    public LoggerService loggerService() {
        return new LoggerService();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor());
    }
}


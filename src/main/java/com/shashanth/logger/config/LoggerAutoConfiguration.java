package com.shashanth.logger.config;

import com.shashanth.logger.filter.RequestResponseLoggingFilter;
import com.shashanth.logger.service.LoggerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LoggerService loggerService() {
        return new LoggerService();
    }

    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> requestResponseLoggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestResponseLoggingFilter());
        registrationBean.addUrlPatterns("/*"); // Apply to all URLs
        registrationBean.setOrder(1); // High priority (before others if needed)
        return registrationBean;
    }
}


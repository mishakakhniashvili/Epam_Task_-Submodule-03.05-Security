package com.epam.gymcrm.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.epam.gymcrm")
@PropertySource("classpath:application.properties")
@EnableTransactionManagement
public class WebConfig implements WebMvcConfigurer {

    private final TransactionIdInterceptor transactionIdInterceptor;

    public WebConfig(TransactionIdInterceptor transactionIdInterceptor) {
        this.transactionIdInterceptor = transactionIdInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(transactionIdInterceptor);
    }
}
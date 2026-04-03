package com.tradejournal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig — Configure static resource serving for frontend
 * 
 * Maps the frontend files (index.html, app.js, style.css) to be served
 * from src/main/resources/static/ at http://localhost:8080/
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static files from classpath:static/ directory
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .setCachePeriod(3600);

        // Serve index.html for root path
        registry.addResourceHandler("/")
            .addResourceLocations("classpath:/static/index.html")
            .setCachePeriod(0);
    }
}

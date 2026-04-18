// src/main/java/com/regnify/config/CorsConfig.java
package com.regnify.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;
    
    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;
    
    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;
    
    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;
    
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Set allowed origins
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOriginPatterns(origins);
        
        // Set allowed methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        config.setAllowedMethods(methods);
        
        // Set allowed headers
        List<String> headers = Arrays.asList(allowedHeaders.split(","));
        config.setAllowedHeaders(headers);
        
        // Set exposed headers
        config.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Credentials"
        ));
        
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

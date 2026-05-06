package com.internship.tool.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
//code
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "policies",
            "policies-list",
            "policy-search"
        );
    }
}//done
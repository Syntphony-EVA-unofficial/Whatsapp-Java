package com.nttdata.eva.whatsapp.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.nttdata.eva.whatsapp.service.CacheManager;

@Configuration
public class AppConfig {
    @Bean
    public CacheManager cacheManager() {
        return new CacheManager();
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

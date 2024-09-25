package com.nttdata.eva.whatsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.nttdata.eva.whatsapp.service.CacheManager;

@SpringBootApplication
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Application Running!" );
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CacheManager cacheManager() {
        return new CacheManager();
    }
}


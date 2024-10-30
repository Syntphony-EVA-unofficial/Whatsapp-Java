package com.nttdata.eva.whatsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Application Running!" );
        SpringApplication.run(App.class, args);
    }


}



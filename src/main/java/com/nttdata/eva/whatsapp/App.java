package com.nttdata.eva.whatsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Application Running!" );
        SpringApplication.run(App.class, args);
    }


}



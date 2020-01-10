package com.wsy.apm.us;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@EnableCaching
@SpringBootApplication
public class SearchUsBootstrap {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SearchUsBootstrap.class).run(args);
    }
}

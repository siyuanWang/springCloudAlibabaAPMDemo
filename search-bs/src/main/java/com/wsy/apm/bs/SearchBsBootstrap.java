package com.wsy.apm.bs;

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
public class SearchBsBootstrap {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SearchBsBootstrap.class).run(args);
    }
}

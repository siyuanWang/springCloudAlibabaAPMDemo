package com.mfw.search.gateway;

import com.mfw.search.gateway.filter.PostBodyCacheFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
 * 本地调用线上网关代理域名：http://search-gateway-aitech.mfwdev.com/jd?token=dd9e46d0eb1aced7
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SearchGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchGatewayApplication.class, args);
    }

    @Bean
    public PostBodyCacheFilter postBodyCacheFilter() {
        return new PostBodyCacheFilter();
    }
}

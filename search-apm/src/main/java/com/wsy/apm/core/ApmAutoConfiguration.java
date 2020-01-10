package com.wsy.apm.core;

import com.wsy.apm.core.aop.ApmAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties
@ConditionalOnProperty(value = "apm.enable", matchIfMissing = true)
public class ApmAutoConfiguration {
    @Bean
    public ApmAspect apmAspect() {
        return new ApmAspect();
    }
}

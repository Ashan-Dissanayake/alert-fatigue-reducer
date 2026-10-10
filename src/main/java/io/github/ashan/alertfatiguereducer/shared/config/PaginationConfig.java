package io.github.ashan.alertfatiguereducer.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.context.annotation.Bean;

@Configuration
public class PaginationConfig {

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> resolver.setMaxPageSize(100);
    }
}
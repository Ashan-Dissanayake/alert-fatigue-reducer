package io.github.ashan.alertfatiguereducer.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI alertFatigueReducerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Alert Fatigue Reducer API")
                        .version("1.0.0")
                        .description("""
                                REST API for rule-based monitoring alert correlation
                                and incident management.

                                Incoming alerts are evaluated using explainable
                                correlation rules and either attached to a matching
                                open incident or used to create a new incident.
                                """)
                        .contact(new Contact()
                                .name("Ashan Dissanayake")
                                .url("https://github.com/Ashan-Dissanayake")));
    }
}

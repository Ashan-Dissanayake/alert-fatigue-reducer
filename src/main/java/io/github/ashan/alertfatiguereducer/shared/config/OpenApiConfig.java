package io.github.ashan.alertfatiguereducer.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH_SCHEME = "basicAuth";

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
                                .url("https://github.com/Ashan-Dissanayake")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BASIC_AUTH_SCHEME, new SecurityScheme()
                                .name(BASIC_AUTH_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("HTTP Basic authentication using credentials configured in application properties")));
    }
}

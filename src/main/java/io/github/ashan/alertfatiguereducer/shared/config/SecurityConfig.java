package io.github.ashan.alertfatiguereducer.shared.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(@Autowired(required = false) RateLimitingFilter rateLimitingFilter) {
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            @Value("${app.security.username:admin}") String adminUsername,
            @Value("${app.security.password:password123}") String adminPassword,
            @Value("${app.security.operator.username:operator}") String operatorUsername,
            @Value("${app.security.operator.password:operator123}") String operatorPassword,
            @Value("${app.security.ingestion.username:ingestion}") String ingestionUsername,
            @Value("${app.security.ingestion.password:ingestion123}") String ingestionPassword,
            PasswordEncoder passwordEncoder) {

        var adminUser = User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN", "OPERATOR", "INGESTION")
                .build();

        var operatorUser = User.withUsername(operatorUsername)
                .password(passwordEncoder.encode(operatorPassword))
                .roles("OPERATOR")
                .build();

        var ingestionUser = User.withUsername(ingestionUsername)
                .password(passwordEncoder.encode(ingestionPassword))
                .roles("INGESTION")
                .build();

        return new InMemoryUserDetailsManager(adminUser, operatorUser, ingestionUser);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/alerts").hasAnyRole("INGESTION", "OPERATOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/incidents/*/resolve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/incidents/**").hasAnyRole("OPERATOR", "ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        if (rateLimitingFilter != null) {
            http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}

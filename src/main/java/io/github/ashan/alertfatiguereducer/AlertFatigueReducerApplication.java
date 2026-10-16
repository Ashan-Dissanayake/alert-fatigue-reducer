package io.github.ashan.alertfatiguereducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AlertFatigueReducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertFatigueReducerApplication.class, args);
    }

}

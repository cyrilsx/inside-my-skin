package dev.healthhub.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "dev.healthhub")
@ConfigurationPropertiesScan(basePackages = "dev.healthhub")
public class HealthHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthHubApplication.class, args);
    }
}

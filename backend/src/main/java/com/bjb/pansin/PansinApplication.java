package com.bjb.pansin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {KafkaAutoConfiguration.class})
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableCaching
@EnableScheduling
@EnableAsync
public class PansinApplication {

    public static void main(String[] args) {
        SpringApplication.run(PansinApplication.class, args);
    }
}

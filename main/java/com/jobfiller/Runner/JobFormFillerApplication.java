package com.jobfiller.Runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EntityScan("com.jobfiller.model")
@SpringBootApplication(scanBasePackages = "com.jobfiller")
@EnableJpaRepositories(basePackages="com.jobfiller.repositories")
public class JobFormFillerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobFormFillerApplication.class, args);
    }
}

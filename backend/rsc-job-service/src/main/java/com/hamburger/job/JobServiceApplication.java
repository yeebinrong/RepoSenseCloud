package com.hamburger.job;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hamburger.common", "com.hamburger.job"})
public class JobServiceApplication {
    public static void main(String[] args) {
        System.out.println(getServiceName() + " starting...");
        SpringApplication app = new SpringApplication(JobServiceApplication.class);

        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("server.port", "3002");
        defaultProperties.put("spring.application.name", "JobService");
        defaultProperties.put("logging.level.root", "INFO");
        app.setDefaultProperties(defaultProperties);

        app.run(args);
    }

    protected static String getServiceName() {
        return "Job Service";
    }
}

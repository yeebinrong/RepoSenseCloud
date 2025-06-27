package com.hamburger.job;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

import static org.junit.jupiter.api.Assertions.*;

public class JobServiceApplicationTest {
    @Test
    void testGetServiceName() {
        assertEquals("Job Service", JobServiceApplication.getServiceName());
    }

    @Test
    void testCorsConfigurer_withEnvVars() throws Exception {
        new EnvironmentVariables(
                "FRONTEND_ORIGIN", "http://localhost:3000",
                "REACT_APP_USER_SERVICE_URL", "http://localhost:8081"
        ).execute(() -> {
            WebMvcConfigurer configurer = new JobServiceApplication().corsConfigurer();
            CorsRegistry registry = new CorsRegistry();
            configurer.addCorsMappings(registry);
            // No exception means success
        });
    }

    @Test
    void testCorsConfigurer_withNullEnvVars() throws Exception {
        new EnvironmentVariables(
                "FRONTEND_ORIGIN", null,
                "REACT_APP_USER_SERVICE_URL", null
        ).execute(() -> {
            WebMvcConfigurer configurer = new JobServiceApplication().corsConfigurer();
            CorsRegistry registry = new CorsRegistry();
            configurer.addCorsMappings(registry);
            
            // No exception means success
        });
    }

    @Test
    void testMain_runsWithoutException() throws Exception {
        new EnvironmentVariables("JOB_SVC_PORT", "8080").execute(() -> {
            // Just check that main does not throw
            JobServiceApplication.main(new String[]{});
        });
    }
}

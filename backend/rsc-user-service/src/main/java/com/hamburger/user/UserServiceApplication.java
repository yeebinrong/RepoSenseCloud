package com.hamburger.user;

import java.util.HashMap;
import java.util.Map;

import com.hamburger.user.middleware.JwtAuthorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hamburger.common", "com.hamburger.user"})
public class UserServiceApplication {
    public static void main(String[] args) {
        System.out.println(getServiceName() + " starting...");
        SpringApplication app = new SpringApplication(UserServiceApplication.class);

        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("server.port", System.getenv("USER_SVC_PORT"));
        defaultProperties.put("spring.application.name", "UserService");
        defaultProperties.put("logging.level.root", "INFO");
        app.setDefaultProperties(defaultProperties);

        app.run(args);
    }

    protected static String getServiceName() {
        return "User Service";
    }

    @Bean
    public FilterRegistrationBean<JwtAuthorization> JwtAuthorization() {
        FilterRegistrationBean<JwtAuthorization> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtAuthorization());
        registrationBean.addUrlPatterns("/api/user/*");
        return registrationBean;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
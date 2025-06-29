package com.hamburger.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserServiceApplicationTest {

    @Autowired
    private UserServiceApplication userServiceApplication;

    @Test
    void testContextLoads() {
        assertThat(userServiceApplication).isNotNull();
    }

    @Test
    void testGetServiceName() {
        assertThat(UserServiceApplication.getServiceName()).isEqualTo("User Service");
    }

    @Test
    void testJwtAuthorizationBean() {
        FilterRegistrationBean<?> bean = userServiceApplication.JwtAuthorization();
        assertThat(bean).isNotNull();
        assertThat(bean.getUrlPatterns()).contains("/api/user/*");
    }

    @Test
    void testCorsConfigurerBean() {
        WebMvcConfigurer configurer = userServiceApplication.corsConfigurer();
        assertThat(configurer).isNotNull();
    }
}
